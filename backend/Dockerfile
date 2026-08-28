FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/workspace-backend-1.0.0.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
