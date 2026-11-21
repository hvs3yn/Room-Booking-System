# Room Booking System — Team Task Plan

**Team:** Onur · Huseyn · Aydan · Mehdi
**Repository:** https://github.com/hvs3yn/Room-Booking-System
**Stack:** Java 21 + Spring Boot 3.5 + PostgreSQL on Neon + HTML/CSS/JS + JUnit 5 (details in [docs/SPEC.md](docs/SPEC.md))

---

## 1. Who does what

| Member | Area | Branch | Task file |
|--------|------|--------|-----------|
| **Onur** | Project setup (Maven, Spring Boot), Neon PostgreSQL, schema, entities, repositories, exceptions, README, seed data | `onur/data-layer` | [tasks/ONUR.md](tasks/ONUR.md) |
| **Huseyn** | Business logic: validation, double-booking rule, free-room search + main unit tests | `huseyn/business-logic` | [tasks/HUSEYN.md](tasks/HUSEYN.md) |
| **Aydan** | REST API: controllers, DTOs, error handling, Swagger + API tests | `aydan/api` | [tasks/AYDAN.md](tasks/AYDAN.md) |
| **Mehdi** | Web frontend (HTML/CSS/JS) + requirements PDF + screenshots + demo checklist | `mehdi/frontend` | [tasks/MEHDI.md](tasks/MEHDI.md) |

Each member creates **their own branch** from `main` (commands are in their task file).

Shared by everyone:
- `docs/AI_USAGE_REPORT.md` — each member writes **only their own section**.
- `docs/REQUIREMENTS.md` — the business requirements document (already written; Mehdi exports the PDF).
- `docs/SPEC.md` — the technical contract. Change it only through a Pull Request the whole team sees.

## 2. How to use your task file with AI

Each `tasks/<NAME>.md` file is **self-contained**: it contains your task, the rules for the AI,
and a full copy of the technical contract. Give the whole file to your AI assistant and add nothing:

- Claude Code / Cursor / Copilot (repository open): `Do the task in tasks/<NAME>.md`
- Chat AI (ChatGPT, Claude.ai…): paste the whole file.

Then **read and understand every line** the AI writes. The teacher will ask each of us questions
about any part of the app. Each task file ends with "Questions you must be able to answer" —
also read the other members' questions before the presentation.

## 3. Neon database

Onur creates the Neon project and sends `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` to the team
**privately** (never commit them). Everyone puts them into their IntelliJ Run Configuration
(or terminal environment). Tests do **not** need Neon — they use an in-memory H2 database.

## 4. Order of work and merging

All four can start **on day 1 in parallel**, because every class name and method signature is fixed in `docs/SPEC.md`.

```
Week 1  Onur: Maven project + Neon + entities + repositories   (finish first!)
        Huseyn: TimeRules + services with Mockito tests (mocks the repositories)
        Aydan: DTOs + controllers + exception handler with @WebMvcTest (mocks the services)
        Mehdi: HTML/CSS layout + api.js + app.js (can test with fake data first)
Week 2  merge Onur → merge Huseyn → merge Aydan (+ integration test)
Week 3  Mehdi connects the UI to the real API → merge; everyone tests the full app on Neon and fixes bugs
Week 4  Requirements PDF, AI usage report, screenshots, rehearse the demo and teacher questions
```

**Merge order into `main`:** 1) Onur → 2) Huseyn → 3) Aydan → 4) Mehdi.
Note: Huseyn's and Aydan's code only **compiles** after Onur's branch is merged (they need the
entities/repositories/exceptions). Until then they can merge `onur/data-layer` into their own branch
locally (`git fetch` then `git merge origin/onur/data-layer`) to compile and run their tests.

After each merge into `main`, everybody runs:

```bash
git checkout main
git pull
git checkout <your-branch>
git merge main
```

## 5. Git rules (required by the course)

- Every member uses their **own GitHub account** (accept the collaborator invitation).
- Never commit directly to `main`. Work on your branch and open a Pull Request.
- At least **3 commits** and **1 merged Pull Request** per member (commit small steps — more is better).
- Every PR is reviewed by one other member: Onur ↔ Huseyn, Aydan ↔ Mehdi (reviewer runs `mvnw test` and leaves a comment).
- Commit messages: `feat: ...`, `fix: ...`, `test: ...`, `docs: ...`, `refactor: ...`, `chore: ...`.

First-time setup for each member:

```bash
git clone https://github.com/hvs3yn/Room-Booking-System.git
cd Room-Booking-System
git config user.name "Your Name"
git config user.email "your-github-email@example.com"
git checkout -b <your-branch>
```

Tools to install: **JDK 21** (e.g. Temurin), **IntelliJ IDEA Community**, **Git**. Maven is not needed (the project has the Maven Wrapper).

## 6. Definition of Done (whole project)

- [ ] App starts with `mvnw spring-boot:run` (with Neon env variables) and the UI opens at http://localhost:8080/
- [ ] The 4 basic requirements work in the UI: add room, book room, no double booking, free rooms
- [ ] `mvnw test` passes (all green)
- [ ] Swagger at http://localhost:8080/swagger-ui.html shows all endpoints
- [ ] Data is in Neon and stays after restarting the app
- [ ] No passwords in Git (`git log -p` shows no real `DB_PASSWORD`)
- [ ] `docs/REQUIREMENTS.pdf` exported and committed
- [ ] `docs/AI_USAGE_REPORT.md` has all 4 sections filled
- [ ] README explains install, Neon setup, run and test
- [ ] Every member has commits + a merged PR, and can explain every file
