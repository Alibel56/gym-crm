FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY workload-service/build.gradle ./workload-service/
COPY discovery-service/build.gradle ./discovery-service/

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY discovery-service/src ./discovery-service/src

RUN ./gradlew --no-daemon :discovery-service:bootJar -x test \
    && cp "$(ls discovery-service/build/libs/*.jar | grep -v plain)" /app/app.jar


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/app.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
