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

# JVM tuned to fit Render free tier's 512MB container.
# UseSerialGC uses far less memory/CPU overhead than the default G1 GC,
# which matters a lot on a single shared vCPU with tight RAM.
ENV JAVA_OPTS="-Xmx320m -Xms128m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom -Dspring.jmx.enabled=false"

COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]