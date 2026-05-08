#
# Build stage
#
FROM maven:3.9.5-eclipse-temurin-21 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests=true


FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

RUN addgroup -g 1000 appgroup && \
    adduser -D -u 1000 -G appgroup appuser

# Switch BEFORE copying files
USER appuser

# Now files are created as appuser automatically
# Copy the built jar
COPY --chown=appuser:appgroup --from=build /home/app/target/vestaboard-service.jar /app/vestaboard-service.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/vestaboard-service.jar"]