# Viva Presentation Script

**Project:** Containerized Sports Facility Booking System
**Student:** Tanisha Sharma (23102C0077), BE CMPN C

> How to use this: each part says **what to SAY** and **what to SHOW** on screen.
> Speak slowly. You don't need to say it word for word — just follow the order.
> Total time: about 10 minutes. For deeper answers, see [docs/viva.md](docs/viva.md).

---

## Before the viva (do this 10 minutes early)

1. Open **Docker Desktop** and wait until it is green / "running".
2. Open these tabs in Chrome (keep them ready, in this order):

| Tab | URL | What it is |
|-----|-----|-----------|
| 1 | https://github.com/tanishasharma1827/23102C0077_devops | My code on GitHub |
| 2 | http://localhost:8081 | The website (running on my laptop) |
| 3 | http://localhost:8080/job/sports-booking-pipeline/ | Jenkins pipeline |
| 4 | http://localhost:8082/sports-booking/ | Staging server (Tomcat) |
| 5 | http://localhost:8090 | Production server (Docker) |
| 6 | http://localhost:8090/actuator/info | Shows which version is live |
| 7 | http://localhost:5000/v2/sports-booking/tags/list | Docker registry (all versions) |
| 8 | http://localhost:8095 | Server set up by Ansible |

3. If any page does not open, see **"If something breaks"** at the bottom.

---

## The whole project in one picture (remember this!)

```text
  I write code
      |
      |  git push
      v
  GITHUB  (stores the code)
      |
      |  Jenkins checks GitHub every 2 minutes
      v
  JENKINS PIPELINE (a robot that does everything automatically)
      1. Checkout        -> download the latest code
      2. Build           -> compile the Java code
      3. Unit Tests      -> 27 tests check the code from inside
      4. Package         -> make one file: sports-booking.war
      5. Deploy Tomcat   -> put it on a TEST server (staging, port 8082)
      6. Selenium Tests  -> a robot opens Chrome and clicks like a user (5 tests)
             |
             |-- if any test FAILS -> STOP. Production is not touched.
             v
      7. Docker Build    -> pack the app in a Docker image (version 1.0.0-b17)
      8. Push Registry   -> save the image in the registry (port 5000)
      9. Deploy Container-> start the LIVE server (production, port 8090)

  ANSIBLE (separate) -> sets up a fresh Linux server automatically (port 8095)
                        and goes back to the old version if the new one is broken
```

**One line:** *"I push code, and in a few minutes it is built, tested and live — with no manual steps.
If a test fails, users keep the old working version."*

---

## PART 1 — Introduction (1 minute)

**SAY:**
> "Good morning Ma'am. My project is the **Containerized Sports Facility Booking System**.
>
> **The problem:** in colleges and clubs, sports courts are booked in a paper register or by phone.
> This causes **double booking**, players can't see which slots are free, and the manager does a lot
> of manual work.
>
> **My solution has two parts.**
> **Part one** is a web application in **Java Spring Boot**. Players register, see free slots and book them.
> The manager confirms or rejects. It never allows double booking and never allows booking a past date.
>
> **Part two** is a full **DevOps pipeline**: **Git, GitHub, Jenkins, Selenium, Docker and Ansible**.
> When I push code, Jenkins builds it, tests it, deploys it to a test server, tests it again in a real
> browser, and only then puts it live in a Docker container."

---

## PART 2 — Show the website (2 minutes)

**SHOW:** Tab 2 (http://localhost:8081)

| Step | Do this | Say this |
|------|---------|----------|
| 1 | Home page | "This is the home page. It shows the 6 facilities and live numbers." |
| 2 | Click **Register**, fill name, email, 10-digit phone | "A player registers. The email must be valid and the phone must be 10 digits." |
| 3 | Go to **Facilities**, pick one, pick today/tomorrow | "Here are the free slots for this facility on this date." |
| 4 | Click **Book** on a free slot | "I book it. The status is **REQUESTED** — waiting for the manager." |
| 5 | Open **My Dashboard** | "My dashboard shows my next game, upcoming bookings and history." |
| 6 | On slots page choose a **past date** | "If I choose a past date, it says **Booking not possible**. This rule is checked on the server too, so nobody can cheat." |
| 7 | Sign out, sign in as `manager@sports.local` | "Now I am the manager." |
| 8 | **Manager Dashboard → Requests** | "Requests are sorted **first come, first served** — oldest first. I can **Confirm** or **Reject**." |
| 9 | Click **Confirm** | "Now the booking is **CONFIRMED**." |
| 10 | Open **Insights** | "These charts show bookings per day, popular facilities and peak hours." |

**Key line:** *"A slot is locked as soon as the first person requests it, so a second person can never book
the same slot. That is double-booking prevention."*

---

## PART 3 — Show the code on GitHub (1 minute)

**SHOW:** Tab 1 (GitHub)

**SAY:**
> "All my code is on GitHub. I used **branches**:
> - **main** = final stable code,
> - **develop** = latest working code — Jenkins builds this,
> - **feature/** branches = one new feature each.
>
> When a feature is ready, I merge it into develop. I also handled a real **merge conflict** in Task 6,
> and tagged the first release as **v1.0.0**.
>
> Important files: the **Jenkinsfile** (the pipeline), the **Dockerfile** (the container recipe) and the
> **ansible** folder (server setup). All my task reports are in the **docs** folder."

**SHOW:** click `Jenkinsfile` briefly, then `Dockerfile`, then `ansible/`.

---

## PART 4 — Show Jenkins (3 minutes) ★ most important

**SHOW:** Tab 3 (Jenkins → sports-booking-pipeline)

**SAY:**
> "This is **Jenkins**, my CI/CD server. It runs on port 8080.
> I have two jobs: **sports-booking-ci**, a simple Freestyle job that builds and tests, and
> **sports-booking-pipeline**, the full pipeline written as code in the **Jenkinsfile**."

**POINT AT** the stage view (the coloured boxes):
> "These are the **9 stages**: Checkout, Build, Unit Tests, Package, Deploy to Tomcat, Selenium Tests,
> Docker Build, Push to Registry, Deploy Container. Green means passed."

**OPEN** a build → **Console Output**, scroll to the top:
> "It says **'Started by an SCM change'**. That means I did not click anything — Jenkins checked GitHub
> every 2 minutes, saw my new commit, and started by itself."

**OPEN** the test result / trend graph:
> "27 unit tests and 5 Selenium tests — **32 tests, all passing**."

**SHOW** build **#8** (red) — this is your best story:
> "In Task 10 I put a bug on purpose: a wrong link in the menu. The **unit tests still passed** — they
> didn't notice. But the **Selenium tests failed**, because a real browser clicked the link and got an
> error page. So build #8 **stopped**, and **production was NOT updated**. I fixed the link, and build #9
> was green. This proves the pipeline protects users from bad code."

---

## PART 5 — Show where the app runs (1 minute)

| Tab | Say |
|-----|-----|
| 4 — staging :8082 | "This is **staging**, a test copy on **Tomcat**. Selenium tests run here first." |
| 5 — production :8090 | "This is **production**, the live app in a **Docker container**." |
| 6 — /actuator/info | "This shows exactly which version is live: image **1.0.0-b17**, from Jenkins build 17." |
| 7 — registry :5000 | "This is my **Docker registry**. It keeps every version, so I can go back to any old one." |

**SAY about Docker:**
> "**Docker** packs my app together with Java into an **image**. A running image is a **container**.
> It runs the same on any machine — no more 'it works on my machine'. My container runs as a
> **non-root user** for safety and has a **health check**."

---

## PART 6 — Ansible (1 minute)

**SHOW:** Tab 8 (http://localhost:8095), then the `ss/task-14` screenshots.

**SAY:**
> "**Ansible** sets up a server automatically using a YAML file called a **playbook**.
> It only needs **SSH** — no software on the server — that's called **agentless**.
>
> Ansible doesn't run on Windows, so I made **two Linux containers**: a **control** node that runs Ansible,
> and a **target** server that Ansible sets up. It installs Java, creates a user, writes the config and
> starts the app.
>
> I did 4 experiments:
> 1. **Fresh server** → everything installed, app UP.
> 2. **Run again** → **changed = 0**. Nothing changed because it was already correct. This is **idempotency**.
> 3. **Deploy a broken version** → health check failed → Ansible **automatically rolled back** to the old version.
> 4. **Kill the app** → run the playbook again → app is back UP."

---

## PART 7 — Closing (30 seconds)

**SAY:**
> "So to summarise: one `git push` gives a tested, deployed application in a few minutes with zero manual
> steps. Bad code is stopped by tests before it reaches users, every version is saved, and servers are
> set up and recovered automatically.
>
> In the future I would add passwords with Spring Security, a real MySQL database, and deploy to the cloud
> with Kubernetes. Thank you, Ma'am."

---

## The 14 tasks in one line each (if Ma'am asks "what did you do in Task X?")

| Task | In one simple line |
|------|--------------------|
| 1 | **Problem definition** — wrote the problem, users, goals and fixed 15 MVP features (M1–M15). |
| 2 | **Agile planning** — 7 user stories, backlog, 4 sprints, Kanban board, Definition of Done. |
| 3 | **Requirements & architecture** — requirements, diagrams, chose the tech stack, first Spring Boot app. |
| 4 | **Git & GitHub setup** — repo, README, .gitignore, issue/PR templates, branch & commit rules. |
| 5 | **Feature branching** — built registration + slot view on a feature branch, merged to develop. 7 tests. |
| 6 | **MVP + collaboration** — finished booking flow, solved a real merge conflict, tag v1.0.0. 19 tests. |
| 7 | **Jenkins CI** — installed Jenkins, Freestyle job builds + tests every 2 min automatically. |
| 8 | **Pipeline as code** — wrote the Jenkinsfile, deployed to Tomcat staging with a health check. |
| 9 | **Selenium tests** — 5 browser tests (register, validation, book, no double booking, confirm/cancel). |
| 10 | **Continuous testing** — Selenium runs in Jenkins; failed build #8 blocked production. |
| 11 | **Docker** — wrote the Dockerfile, non-root user, health check, practised container commands. |
| 12 | **Jenkins + Docker CD** — Jenkins builds image, pushes to registry, deploys production container. |
| 13 | **Ansible config** — playbook sets up a Linux server (Java, user, config, service). |
| 14 | **Provisioning & reliability** — deploy with health check, automatic rollback, idempotency proof. |

---

## Top 15 questions — short answers

1. **What is DevOps?** Developers and operations working together, using automation to release faster and safer.
2. **What is CI?** Every code change is automatically built and tested.
3. **What is CD?** Every change that passes tests is automatically deployed. Mine deploys automatically.
4. **What is Jenkins?** A free automation server that runs my build, test and deploy steps.
5. **What is a Jenkinsfile?** The pipeline written as code, saved in Git with the project.
6. **How does Jenkins start?** Poll SCM — it checks GitHub every 2 minutes. (Webhook can't reach localhost.)
7. **What is Selenium?** A tool that controls a real browser to test a website like a user.
8. **What is Docker?** A tool that packs the app with everything it needs into a container.
9. **Image vs container?** Image = recipe/template. Container = the running app made from it.
10. **Container vs VM?** Container shares the host OS, small and starts in seconds. VM has a full OS, big and slow.
11. **What is Ansible?** A tool that sets up servers from a YAML playbook over SSH, with no agent.
12. **What is idempotency?** Running it again changes nothing if everything is already correct.
13. **What is rollback?** Going back to the last working version if the new one breaks.
14. **Staging vs production?** Staging = test copy. Production = what real users use.
15. **How do you stop double booking?** Before saving, the server checks if the slot already has an active booking. The method is `synchronized`, so two people at the same moment can't both pass.

**If you don't know an answer, say:** *"I'm not fully sure, Ma'am, but in my project the related part works
like this…"* — and explain what you DO know.

---

## If something breaks during the demo

| Problem | Fix |
|---------|-----|
| Pages on 8090 / 8082 / 5000 don't open | Open Docker Desktop and wait 1 minute. |
| Staging 8082 doesn't open | PowerShell: `docker start tomcat-staging` (wait 30 s) |
| Ansible server 8095 doesn't open | `docker compose -f ansible/docker-compose.yml up -d` then `docker compose -f ansible/docker-compose.yml exec control ansible-playbook deploy.yml -e app_version=1.0.0` |
| Website on 8081 doesn't open | In the project folder: `java -jar target\sports-booking.war --server.port=8081` |
| Anything else | Stay calm and show the screenshots in the `ss/` folder — all the proof is there. |
