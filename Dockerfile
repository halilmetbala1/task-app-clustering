# Optimize JAR
FROM eclipse-temurin:21-jre AS builder
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# Build final image
FROM eclipse-temurin:21-jre
EXPOSE 8081

# Copy layered JAR
WORKDIR /app
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Spring
ENV SPRING_PROFILES_ACTIVE=prod
#ENV SERVER_PORT=8081

## Database
#ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/etutor_binary_search
#ENV SPRING_DATASOURCE_USERNAME=etutor_binary_search
#ENV SPRING_DATASOURCE_PASSWORD=TBD
#ENV SPRING_FLYWAY_USER=etutor_binary_search_admin
#ENV SPRING_FLYWAY_PASSWORD=TBD

## Clients (just as example, add more when executing)
#ENV CLIENTS_API_KEYS_0_NAME=task-administration
#ENV CLIENTS_API_KEYS_0_KEY=TBD
#ENV CLIENTS_API_KEYS_0_ROLES_0=CRUD
#ENV CLIENTS_API_KEYS_0_ROLES_1=SUBMIT

ENTRYPOINT ["java", "-Xmx6g", "org.springframework.boot.loader.launch.JarLauncher"]
