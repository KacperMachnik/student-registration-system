FROM maven:3.9-eclipse-temurin-21

ARG FAT_JAR_FILE=target/student-registration-system-1.0.0.jar

WORKDIR /app

COPY ${FAT_JAR_FILE} application.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]