FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY workload-service/build.gradle ./workload-service/
COPY discovery-service/build.gradle ./discovery-service/

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY src ./src

RUN ./gradlew --no-daemon :bootJar -x test \
    && cp "$(ls build/libs/*.jar | grep -v plain)" /app/app.jar


FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --no-create-home appuser \
    && mkdir -p /app/logs \
    && chown -R appuser:appuser /app

COPY --from=build /app/app.jar app.jar

USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]