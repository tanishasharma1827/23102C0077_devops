# Task 1: Problem Definition and Scope

> **Goal:** Understand the real problem, who has it, and decide a small first
> version (MVP) that we can actually build.
>
> **Deliverables:** Problem statement, stakeholder list, objectives, constraints
> and approved MVP scope.

---

## 1. The real-world problem

Colleges, housing societies and sports clubs have courts and turfs
(badminton, football, cricket nets, table tennis). Today they are usually
booked like this:

- A person calls or walks to the sports office.
- The in-charge checks a **paper register** or a WhatsApp group.
- The slot is written down by hand.

### Pain points

| # | Pain point | Who it hurts |
|---|-----------|--------------|
| 1 | No way to see which slots are free without calling or visiting | Players |
| 2 | **Double booking**: two groups arrive for the same slot | Players, Manager |
| 3 | No record of who booked, so no-shows can't be tracked | Manager |
| 4 | Cancellations are not shared, so freed slots stay empty | Players, Manager |
| 5 | Manager spends a lot of time answering "is it free?" calls | Manager |
| 6 | No data on which facility/time is most used | Admin |

## 2. Problem statement

> Players have no simple way to check and book sports facility slots. Today's
> manual, register-based process causes double bookings, wasted slots and
> extra work for the facility manager. We need a **web application** where
> users can see free slots, request a booking, get confirmation, cancel and
> track status, all in one place.

## 3. Target users and stakeholders

| Stakeholder | Role | What they need |
|-------------|------|----------------|
| **Player / Student** | Main user | See free slots, book, cancel, check status |
| **Facility Manager** | Operator | See all bookings, confirm or reject requests |
| **Admin (Sports Dept.)** | Owner | Add facilities and slot timings, view usage |
| **Developer (me)** | Builder | Clear requirements, working CI/CD pipeline |
| **Faculty Guide** | Reviewer | Evidence of every DevOps task |

## 4. Objectives

1. Let a user **see available slots** for any facility and date.
2. Let a user **request a booking** in under 1 minute.
3. **Prevent double booking**: one slot can have only one confirmed booking.
4. Let a user **cancel** a booking and free the slot instantly.
5. Show the **status** of every booking (Requested / Confirmed / Cancelled).
6. Build, test and deploy the app **automatically** using a DevOps pipeline
   (Git, Jenkins, Selenium, Docker, Ansible).

## 5. Constraints

| Type | Constraint |
|------|-----------|
| **Time** | One semester, with 14 lab tasks |
| **Team** | One developer |
| **Cost** | Only free and open-source tools (GitHub, Jenkins, Docker, Ansible) |
| **Hardware** | Runs on a normal laptop (8 GB RAM) |
| **Technology** | Must be buildable with Maven/Gradle/Ant and deployable to Tomcat/Nginx |
| **Scope** | No online payment, no mobile app, no SMS/email in the MVP |

## 6. Success criteria (how we know it works)

| # | Criterion | Target |
|---|-----------|--------|
| 1 | A user can register and book a slot | End-to-end works |
| 2 | Double booking is blocked | 0 double bookings in tests |
| 3 | Cancelled slot becomes available again | Immediately |
| 4 | Every push to GitHub is built and tested by Jenkins | 100% of commits |
| 5 | The app runs inside a Docker container | `docker run` works |
| 6 | Automated tests cover the main user journeys | 3 to 5 Selenium tests passing |

## 7. MVP scope (frozen at 15 tasks)

The **MVP (Minimum Viable Product)** is the smallest version that solves the
core problem. We freeze it at **15 work items**. Anything else waits for later.

### In scope

| ID | MVP work item | Feature area |
|----|---------------|-------------|
| M1 | Project skeleton (Spring Boot + Maven) | Setup |
| M2 | Home page with app layout | Setup |
| M3 | `User` data model and database table | Registration |
| M4 | User registration form | Registration |
| M5 | `Facility` and `Slot` data models | Availability |
| M6 | Load sample facilities and slots | Availability |
| M7 | Availability page: list free slots by facility and date | Availability |
| M8 | `Booking` data model with status field | Booking |
| M9 | Booking request form | Booking |
| M10 | Double-booking check | Booking |
| M11 | Booking confirmation (manager confirms) | Confirmation |
| M12 | Cancel booking and free the slot | Cancellation |
| M13 | "My Bookings" page with status | Status tracking |
| M14 | Input validation and error messages | Quality |
| M15 | Health check endpoint (`/actuator/health`) | Operations |

### Out of scope (not in MVP)

- Online payment
- Email / SMS notifications
- Mobile app
- Login with Google
- Reports and charts for admin

> **Approved MVP scope:** Items M1 to M15 above.
