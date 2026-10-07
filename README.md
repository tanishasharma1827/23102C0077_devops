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

Open http://localhost:8080. The health check is at http://localhost:8080/actuator/health.

## Documentation

All lab task reports are in the [docs](docs/README.md) folder.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for branch and commit rules.
