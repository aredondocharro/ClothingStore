# ===== Build stage =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline
COPY src/ ./src
RUN mvn -q -e -B -DskipTests package
# Si usas Spring Boot repackage, el jar suele quedar en target/*.jar
# Ajusta el nombre si tu jar no se llama app.jar
RUN cp target/*.jar /app/app.jar

# ===== Runtime stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar /app/app.jar
EXPOSE 8081
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
