FROM gradle:8.7-jdk17 as build
WORKDIR /app
COPY . .
# Build usando Gradle 8 para suportar dependências recentes
RUN gradle clean bootJar --no-daemon --stacktrace

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/agendador-tarefas.jar

EXPOSE 8081

CMD ["java", "-jar", "/app/agendador-tarefas.jar"]
