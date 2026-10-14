# Containerized Sports Facility Booking System

A web application to view, book, confirm and cancel sports facility slots
(badminton courts, football turfs, etc.), built and delivered with a full
DevOps pipeline: **Git → Jenkins → Selenium → Docker → Ansible**.

**Student:** Tanisha Sharma (23102C0077), BE CMPN C, 2026-27 ODD Sem

## Features (MVP)

- User registration
- View available slots by facility and date
- Request a booking (double booking is blocked)
- Manager confirmation
- Cancellation
- Booking status tracking

## Tech stack

Java 17 · Spring Boot 3 · Thymeleaf · H2 · Maven · Tomcat · Jenkins · Selenium · Docker · Ansible

## Run locally

Requirements: JDK 17+ and Maven 3.9+

```bash
mvn clean package        # build and run tests
mvn spring-boot:run      # start the app
```

Open http://localhost:8081 (8081 locally because Jenkins uses 8080). The health check is at http://localhost:8081/actuator/health.

## CI/CD pipeline

Every push to `develop` is picked up by Jenkins (SCM polling) and runs the [`Jenkinsfile`](Jenkinsfile):

```text
Checkout → Build → Unit Tests → Package → Deploy to Tomcat (staging)
        → Selenium Tests → Docker Build → Push to Registry → Deploy Container (production)
```

| What | Where |
|------|-------|
| Jenkins | http://localhost:8080 |
| App (local `mvn spring-boot:run`) | http://localhost:8081 |
| Staging (Tomcat) | http://localhost:8082/sports-booking/ |
| Production (Docker container) | http://localhost:8090 |
| Docker registry | http://localhost:5000/v2/_catalog |
| Ansible-provisioned server | http://localhost:8095 |

## Documentation

All lab task reports are in the [docs](docs/README.md) folder.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for branch and commit rules.
