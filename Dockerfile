# Sports Facility Booking System - container image (Task 11)
# Build the WAR first:   mvn clean package
# Then build the image:  docker build -t sports-booking:1.0.0 .

FROM eclipse-temurin:17-jre

LABEL org.opencontainers.image.title="sports-booking" \
      org.opencontainers.image.description="Containerized Sports Facility Booking System" \
      org.opencontainers.image.authors="Tanisha Sharma (23102C0077)" \
      org.opencontainers.image.source="https://github.com/tanishasharma1827/23102C0077_devops"

# Run as a normal user, not root
RUN groupadd --system app && useradd --system --gid app --home /app app
WORKDIR /app

COPY --chown=app:app target/sports-booking.war app.war

USER app

# Inside the container the app listens on 8080
ENV SERVER_PORT=8080 \
    JAVA_OPTS="-Xms128m -Xmx384m"
EXPOSE 8080

# Docker marks the container healthy/unhealthy using the Spring Boot health endpoint
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fs http://localhost:8080/actuator/health | grep -q UP || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.war"]
