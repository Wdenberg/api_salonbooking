# Stage 1: build
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean verify -B -DskipITs

# Stage 2: runtime
FROM eclipse-temurin:25-jre-jammy
RUN groupadd -r spring && useradd -r -g spring -d /app -s /sbin/nologin spring
WORKDIR /app
COPY --from=build /app/target/salonbooking-0.0.1-SNAPSHOT.jar app.jar
RUN chown spring:spring app.jar
USER spring:spring
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", "app.jar"]