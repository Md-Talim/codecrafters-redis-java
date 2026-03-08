# Build stage
FROM maven:3.9-eclipse-temurin-23 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -B package -Ddir=/app/target

# Runtime stage
FROM eclipse-temurin:23-jre

# Installing nc for healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends netcat-openbsd && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/codecrafters-redis.jar /app/redis.jar

EXPOSE 6380

ENTRYPOINT ["java", "-jar", "/app/redis.jar"]
