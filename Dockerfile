# Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV PORT=8080
EXPOSE $PORT

# JVM optimization - More memory for faster startup
ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom -Dspring.jmx.enabled=false"

COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]