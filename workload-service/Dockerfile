FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY workload-service/build.gradle ./workload-service/
COPY discovery-service/build.gradle ./discovery-service/

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY workload-service/src ./workload-service/src

RUN ./gradlew --no-daemon :workload-service:bootJar -x test \
    && cp "$(ls workload-service/build/libs/*.jar | grep -v plain)" /app/app.jar


FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --no-create-home appuser

COPY --from=build /app/app.jar app.jar

USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
