'use strict';

/* ============================================================
   Gym CRM — фронт на чистом JS.
   Бэкенд: Spring Boot, context-path /gym, REST под /gym/api, JWT (Bearer).
   Если страница открыта не с самого Spring (file://, Live Server и т.п.),
   запросы идут на http://localhost:8080 — тогда нужен CORS (см. SecurityConfig).
   ============================================================ */
const API = (location.pathname.startsWith('/gym') ? '' : 'http://localhost:8080') + '/gym/api';

const $  = (s, r = document) => r.querySelector(s);
const $$ = (s, r = document) => [...r.querySelectorAll(s)];
const esc = v => String(v ?? '').replace(/[&<>"']/g,
    c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const enc = encodeURIComponent;
const formObj = f => Object.fromEntries(new FormData(f));
const today = () => new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10);

const state = { token: null, username: null, role: null, profile: null, types: [], typesById: {} };

const store = {
    save()  { try { sessionStorage.setItem('gym', JSON.stringify({ token: state.token, username: state.username, role: state.role })); } catch {} },
    load()  { try { return JSON.parse(sessionStorage.getItem('gym')); } catch { return null; } },
    clear() { try { sessionStorage.removeItem('gym'); } catch {} }
};

/* ---------------- UI helpers ---------------- */
let toastTimer;
function toast(msg, type = 'ok') {
    const t = $('#toast');
    t.textContent = msg;
    t.className = `toast show ${type}`;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => t.classList.remove('show'), 3500);
}
const authVisible = () => $('#auth').classList.contains('visible');
function showAuthError(msg) {
    const el = $('#auth-error');
    el.textContent = msg;
    el.classList.toggle('hidden', !msg);
}
function onSubmit(form, handler) {
    form.addEventListener('submit', async e => {
        e.preventDefault();
        const btn = form.querySelector('button:not([type=button])');
        if (btn) btn.disabled = true;
        showAuthError('');
        try { await handler(formObj(form), form); }
        catch (err) { authVisible() ? showAuthError(err.message) : toast(err.message, 'error'); }
        finally { if (btn) btn.disabled = false; }
    });
}

/* ---------------- HTTP ---------------- */
async function api(method, path, body) {
    const headers = {};
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    if (state.token) headers.Authorization = 'Bearer ' + state.token;

    let res;
    try {
        res = await fetch(API + path, { method, headers, body: body !== undefined ? JSON.stringify(body) : undefined });
    } catch {
        throw Object.assign(new Error('Сервер недоступен'), { status: 0 });
    }

    const text = await res.text();
    let data = null;
    try { data = text ? JSON.parse(text) : null; } catch { /* не JSON */ }

    if (!res.ok) {
        let msg = data?.message || `Ошибка ${res.status}`;
        if (data?.validationErrors?.length) msg += ': ' + data.validationErrors.join('; ');
        // 401 на /auth/* — это «неверный пароль», а не истёкший токен
        if (res.status === 401 && state.token && !path.startsWith('/auth/')) {
            logout('Сессия истекла, войдите снова', false);
        }
        throw Object.assign(new Error(msg), { status: res.status });
    }
    return data;
}

/* ---------------- Training types ---------------- */
const typeName = id => state.typesById[id] ?? (id == null ? '—' : '#' + id);

async function loadTypes() {
    try {
        const r = await api('GET', '/training-types');
        state.types = r?.trainingTypes ?? [];
    } catch { state.types = []; }
    state.typesById = Object.fromEntries(state.types.map(t => [t.trainingTypeId, t.trainingTypeName]));

    // регистрация тренера: select, а если список недоступен (нет токена и эндпоинт закрыт) — поле с ID
    const sel = $('#reg-spec'), num = $('#reg-spec-num'), ok = state.types.length > 0;
    sel.innerHTML = state.types.map(t => `<option value="${esc(t.trainingTypeId)}">${esc(t.trainingTypeName)}</option>`).join('');
    sel.classList.toggle('hidden', !ok); sel.disabled = !ok;
    num.classList.toggle('hidden', ok);  num.disabled = ok;

    $('#flt-type').innerHTML = '<option value="">Любой</option>' +
        state.types.map(t => `<option value="${esc(t.trainingTypeName)}">${esc(t.trainingTypeName)}</option>`).join('');
}

/* ---------------- Auth ---------------- */
$$('.tabs button').forEach(b => b.addEventListener('click', () => {
    $$('.tabs button').forEach(x => x.classList.toggle('active', x === b));
    $$('.auth-form').forEach(f => f.classList.add('hidden'));
    $(b.dataset.auth === 'login' ? '#form-login' : '#form-reg-' + b.dataset.auth).classList.remove('hidden');
    $('#reg-result').classList.add('hidden');
    showAuthError('');
}));

function showRegResult(r) {
    $('#rr-user').textContent = r.userName;
    $('#rr-pass').textContent = r.password;
    $('#reg-result').classList.remove('hidden');
}
$('#rr-login').onclick = () => {
    $('.tabs button[data-auth=login]').click();
    const f = $('#form-login');
    f.userName.value = $('#rr-user').textContent;
    f.password.value = $('#rr-pass').textContent;
    $('#reg-result').classList.add('hidden');
};
$('#rr-copy').onclick = async () => {
    try {
        await navigator.clipboard.writeText(`${$('#rr-user').textContent}\n${$('#rr-pass').textContent}`);
        toast('Скопировано');
    } catch { toast('Не удалось скопировать — выделите вручную', 'error'); }
};

onSubmit($('#form-reg-trainee'), async (d, f) => {
    d.dateOfBirth = d.dateOfBirth || null;
    d.address = d.address || null;
    showRegResult(await api('POST', '/auth/trainee/register', d));
    f.reset();
});
onSubmit($('#form-reg-trainer'), async (d, f) => {
    d.specializationId = Number(d.specializationId);
    showRegResult(await api('POST', '/auth/trainer/register', d));
    f.reset();
});

// Логин возвращает только токен, роли в нём нет — определяем по тому, чей профиль нашёлся.
onSubmit($('#form-login'), async d => {
    try {
        const r = await api('POST', '/auth/login', { userName: d.userName, password: d.password });
        state.token = r.token;
        state.username = d.userName;
        await detectRole();
        store.save();
        await enterApp();
    } catch (err) {
        state.token = null; state.username = null; state.role = null;
        throw err;
    }
});

async function detectRole() {
    try {
        state.profile = await api('GET', '/trainees/' + enc(state.username));
        state.role = 'trainee';
    } catch (e) {
        if (![400, 404].includes(e.status)) throw e;
        state.profile = await api('GET', '/trainers/' + enc(state.username));
        state.role = 'trainer';
    }
}

async function logout(message, callServer = true) {
    if (callServer && state.token) { try { await api('POST', '/auth/logout'); } catch {} }
    Object.assign(state, { token: null, username: null, role: null, profile: null });
    store.clear();
    $('#app').classList.add('hidden');
    $('#auth').classList.add('visible');
    showAuthError(message || '');
    loadTypes();
}
$('#logout').onclick = () => logout();

/* ---------------- App shell ---------------- */
const isTrainee = () => state.role === 'trainee';

async function enterApp() {
    await loadTypes();
    $('#auth').classList.remove('visible');
    $('#app').classList.remove('hidden');
    $$('.trainee-only').forEach(el => el.classList.toggle('hidden', !isTrainee()));
    $$('.trainer-only').forEach(el => el.classList.toggle('hidden', isTrainee()));
    $('#me-name').textContent = state.username;
    $('#me-role').textContent = isTrainee() ? 'Trainee' : 'Trainer';
    $('#th-partner').textContent = isTrainee() ? 'Тренер' : 'Подопечный';
    $('#flt-partner-lbl').textContent = isTrainee() ? 'Username тренера' : 'Username подопечного';
    $('#st-partners-lbl').textContent = isTrainee() ? 'Тренеров' : 'Подопечных';
    renderProfile();
    showPage('dashboard');
    loadTrainings({}, true);
}

function showPage(name) {
    $$('.nav-btn').forEach(b => b.classList.toggle('active', b.dataset.page === name));
    $$('.page').forEach(p => p.classList.toggle('visible', p.id === 'page-' + name));
    if (name === 'trainings') loadTrainings(currentFilter());
    if (name === 'trainers') loadTrainersPage();
    if (name === 'trainees') renderTrainees();
    if (name === 'add-training') prepareTrainingForm();
}
$$('.nav-btn').forEach(b => b.addEventListener('click', () => showPage(b.dataset.page)));

/* ---------------- Profile ---------------- */
function renderProfile() {
    const p = state.profile, f = $('#form-profile');
    $('#pf-username').value = state.username;
    f.firstName.value = p.firstName ?? '';
    f.lastName.value = p.lastName ?? '';
    f.isActive.checked = !!p.isActive;
    if (isTrainee()) {
        f.dateOfBirth.value = p.dateOfBirth ?? '';
        f.address.value = p.address ?? '';
    } else {
        $('#pf-spec').value = typeName(p.specializationId);
    }
    $('#st-status').textContent = p.isActive ? 'Active' : 'Inactive';
    $('#st-partners').textContent = (isTrainee() ? p.trainers : p.trainees)?.length ?? 0;
}

onSubmit($('#form-profile'), async (d, f) => {
    const active = f.isActive.checked;
    if (state.profile.isActive && !active &&
        !confirm('Деактивированный аккаунт не сможет войти в систему. Продолжить?')) return;

    const body = { userName: state.username, firstName: d.firstName, lastName: d.lastName, isActive: active };
    if (isTrainee()) { body.dateOfBirth = d.dateOfBirth || null; body.address = d.address || null; }

    state.profile = await api('PUT', isTrainee() ? '/trainees' : '/trainers', body);
    renderProfile();
    toast('Профиль сохранён');
});

$('#delete-profile').onclick = async () => {
    if (!confirm('Удалить профиль вместе со всеми тренировками? Это необратимо.')) return;
    try {
        await api('DELETE', '/trainees/' + enc(state.username));
        logout('Профиль удалён', false);
    } catch (err) { toast(err.message, 'error'); }
};

/* ---------------- Trainings ---------------- */
let lastParams = {};
function currentFilter() {
    const d = formObj($('#form-filter'));
    const p = { periodFrom: d.periodFrom, periodTo: d.periodTo };
    if (d.partner) p[isTrainee() ? 'trainerName' : 'traineeName'] = d.partner.trim();
    if (isTrainee() && d.trainingType) p.trainingType = d.trainingType;
    return p;
}

async function loadTrainings(params = {}, forStatsOnly = false) {
    const q = new URLSearchParams(Object.entries(params).filter(([, v]) => v));
    const url = `/${isTrainee() ? 'trainees' : 'trainers'}/${enc(state.username)}/trainings` + (q.size ? '?' + q : '');
    try {
        const list = await api('GET', url);
        if (!q.size) $('#st-trainings').textContent = list.length;
        if (forStatsOnly) return;
        lastParams = params;
        renderTrainings(list);
    } catch (err) { if (!forStatsOnly) toast(err.message, 'error'); }
}

function renderTrainings(list) {
    $('#trainings-body').innerHTML = list.length
        ? list.map(t => `
      <tr>
        <td>${esc(t.trainingName)}</td>
        <td>${esc(t.trainingDate)}</td>
        <td>${esc(t.trainingType)}</td>
        <td>${esc(t.trainingDuration)}</td>
        <td>${esc(t.trainerName ?? t.traineeName)}</td>
        <td>${t.id != null ? `<button class="btn danger small" data-cancel="${esc(t.id)}">Отменить</button>` : ''}</td>
      </tr>`).join('')
        : '<tr><td class="empty" colspan="6">Ничего не найдено</td></tr>';
}

$('#form-filter').addEventListener('submit', e => { e.preventDefault(); loadTrainings(currentFilter()); });
$('#flt-reset').onclick = () => { $('#form-filter').reset(); loadTrainings({}); };

// отмена тренировки (кнопка появляется, только если бэкенд отдаёт id тренировки)
$('#trainings-body').addEventListener('click', async e => {
    const btn = e.target.closest('[data-cancel]');
    if (!btn || !confirm('Отменить эту тренировку?')) return;
    try {
        await api('DELETE', '/trainings/' + enc(btn.dataset.cancel));
        toast('Тренировка отменена');
        loadTrainings(lastParams);
        loadTrainings({}, true);
    } catch (err) { toast(err.message, 'error'); }
});

/* ---------------- Trainee: my trainers ---------------- */
async function loadTrainersPage() {
    const body = $('#trainers-body');
    try {
        const unassigned = await api('GET', `/trainees/${enc(state.username)}/unassigned-trainers`);
        const rows = [
            ...(state.profile.trainers ?? []).map(t => ({ ...t, checked: true })),
            ...unassigned.map(t => ({ ...t, checked: false }))
        ];
        body.innerHTML = rows.length
            ? rows.map(t => `
        <tr>
          <td><input type="checkbox" data-user="${esc(t.userName)}" ${t.checked ? 'checked' : ''}></td>
          <td>${esc(t.userName)}</td>
          <td>${esc(t.firstName)} ${esc(t.lastName)}</td>
          <td>${esc(typeName(t.specializationId))}</td>
        </tr>`).join('')
            : '<tr><td class="empty" colspan="4">Тренеров пока нет</td></tr>';
    } catch (err) { toast(err.message, 'error'); }
}

$('#save-trainers').onclick = async () => {
    const chosen = $$('#trainers-body input:checked').map(c => ({ userName: c.dataset.user }));
    if (!chosen.length) return toast('Выберите хотя бы одного тренера', 'error');
    try {
        await api('PUT', '/trainees/trainers', { traineeUserName: state.username, trainersList: chosen });
        state.profile = await api('GET', '/trainees/' + enc(state.username));
        renderProfile();
        loadTrainersPage();
        toast('Список тренеров обновлён');
    } catch (err) { toast(err.message, 'error'); }
};

/* ---------------- Trainer: my trainees ---------------- */
function renderTrainees() {
    const list = state.profile.trainees ?? [];
    $('#trainees-body').innerHTML = list.length
        ? list.map(t => `<tr><td>${esc(t.userName)}</td><td>${esc(t.firstName)} ${esc(t.lastName)}</td></tr>`).join('')
        : '<tr><td class="empty" colspan="2">Пока нет подопечных</td></tr>';
}

/* ---------------- New training ---------------- */
function prepareTrainingForm() {
    const f = $('#form-training');
    const opt = list => (list ?? []).map(u => `<option value="${esc(u.userName)}">${esc(u.firstName)} ${esc(u.lastName)}</option>`).join('');
    $('#dl-trainers').innerHTML = isTrainee() ? opt(state.profile.trainers) : '';
    $('#dl-trainees').innerHTML = isTrainee() ? '' : opt(state.profile.trainees);

    f.traineeUserName.readOnly = isTrainee();
    f.trainerUserName.readOnly = !isTrainee();
    if (isTrainee()) f.traineeUserName.value = state.username;
    else f.trainerUserName.value = state.username;
    if (!f.trainingDate.value) f.trainingDate.value = today();
}

onSubmit($('#form-training'), async (d, f) => {
    d.trainingDuration = Number(d.trainingDuration);
    await api('POST', '/trainings', d);
    toast('Тренировка создана');
    f.reset();
    prepareTrainingForm();
    loadTrainings({}, true);
});

/* ---------------- Password ---------------- */
onSubmit($('#form-password'), async (d, f) => {
    await api('PUT', '/auth/password', { userName: state.username, oldPassword: d.oldPassword, newPassword: d.newPassword });
    f.reset();
    toast('Пароль изменён');
});

/* ---------------- Init ---------------- */
(async function init() {
    const saved = store.load();
    if (saved?.token) {
        Object.assign(state, saved);
        try {
            await detectRole();
            store.save();
            await enterApp();
            return;
        } catch { Object.assign(state, { token: null, username: null, role: null, profile: null }); store.clear(); }
    }
    await loadTypes();
    $('#auth').classList.add('visible');
})();