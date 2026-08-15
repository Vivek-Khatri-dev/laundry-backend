# Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV PORT=8080
EXPOSE $PORT

# JVM optimization for Render
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]