package com.practice.cucumber.support;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

@Component
public class ScenarioContext {

    @Setter
    @Getter
    private ResultActions lastResult;
    private final Map<String, String> generatedCredentials = new HashMap<>();
    private final Map<String, String> usernamesByFullName = new HashMap<>();
    @Setter
    @Getter
    private String issuedToken;

    public void rememberCredentials(String username, String password) {
        generatedCredentials.put(username, password);
    }

    public String passwordFor(String username) {
        return generatedCredentials.get(username);
    }

    public void rememberUsernameForFullName(String fullName, String username) {
        usernamesByFullName.put(fullName, username);
    }

    public String usernameForFullName(String fullName) {
        return usernamesByFullName.get(fullName);
    }

}
