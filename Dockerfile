FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21
WORKDIR /app
COPY --from=build /app/target/student-registration-system-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]