# Task File — Aydan (REST API)

> **How to use this file (for Aydan):**
> Give this whole file to your AI coding assistant. No extra prompt is needed.
> - Claude Code / Cursor / Copilot (repository open): write `Do the task in tasks/AYDAN.md`
> - Chat AI (ChatGPT, Claude.ai…): paste the whole file, then create the files it gives you.
>
> After the AI finishes, **read and understand every line**. The teacher will ask you questions.

---

## 0. Instructions for the AI assistant (read first)

You are a senior Java engineer (up to date with Java 27 and Spring Boot 4 — do not use Spring Boot 3 APIs or imports) and a patient teacher helping **Aydan**, a university student,
build their part of a 4-person team project: a **Room Booking System**
(Java 27 + Spring Boot 4 + PostgreSQL on Neon + HTML/CSS/JS). The other three members build
the other parts **at the same time** on their own Git branches. The parts only fit together if you
follow the contract exactly.

Rules you must follow:

1. **Follow the Technical Contract (Appendix A at the end of this file) exactly**: package names,
   class names, method names, parameter types, return types, HTTP routes, status codes, JSON
   shapes and error messages. Do not rename anything and do not invent a different design.
2. **Only create or edit the files listed in "Files you own"** below. Never edit files owned by
   other members. Classes owned by others that you depend on are defined in the contract; use them
   exactly as described there (they will exist after the other branches are merged).
3. Do **not** add dependencies beyond the contract. No Lombok, no extra frameworks, no login,
   no features that are not in this file.
4. Write clean, simple, beginner-readable code: constructor injection, `private final` fields,
   short Javadoc comments, small methods, clear names, no dead code, no `System.out.println`
   in application code. Prefer clarity over cleverness.
5. Work **step by step** in the order given in "Steps". After each step:
   - show the complete content of every file you created or changed;
   - give the exact Git commands to commit that step (message like `feat: ...`);
   - tell the student how to check that the step works (command and expected result).
6. Start by giving the Git commands to switch to the branch `aydan` (create it from an
   up-to-date `main` if it does not exist).
7. Never write a real database password into any file. Neon credentials only come from the
   environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
8. Finish by giving the commands to push the branch and the text for the Pull Request
   (title + description: what was done, how it was tested).
9. At the end, write **"Explanation for Aydan"**: explain every file you wrote in simple
   English (what it does, why it is designed this way, how data flows through it), then answer
   every question in "Questions you must be able to answer" briefly and clearly.

---

## 0.1 Getting the other members' code (no fixed order)

The four members do **not** work in a fixed order — anybody may finish first. So never assume a
branch exists or is merged; **check first**, at the start and every time you need someone's work:

```bash
git fetch origin
git branch -r                          # which branches exist on GitHub
git branch -r --merged origin/main     # which of them are already merged into main
```

Team branches (exact names, case matters): `Onur`, `huseyn`, `aydan`, `mehdi`.
For **each** member listed under "Depends on" in section 1, do the first case that is true:

| Situation | What to do |
|-----------|------------|
| Their branch is already merged into `main` | `git merge origin/main` (into your branch). |
| Their branch exists on GitHub but is not merged yet | `git merge origin/<branch>` (e.g. `git merge origin/Onur`, `git merge origin/huseyn`) so your code compiles and runs. Their commits keep their author name. Tell them you did it; later, when their PR is merged, just run `git merge origin/main` again. |
| Their branch does not exist yet | Write your code from the contract (Appendix A) — names and signatures are fixed, so it will compile once their code arrives. Unit tests use mocks/fake data, so they do not need the real code. Check again later. |

Rules:
- A Pull Request may be merged into `main` **only when, after `git merge origin/main`, the project compiles and `mvnw test` passes** on that branch. This is the only ordering rule.
- Merge conflicts should not happen because every file has one owner. If one happens in a shared doc
  (e.g. `docs/SPEC.md`), keep **both** sides. Never "fix" a conflict by deleting another member's code — ask them.

---

## 0.2 Use the project's AI toolkit (`.claude/` folder)

The repository contains a `.claude/` folder with **Everything Claude Code (ECC)**: ready-made agents,
skills, slash commands and rules for Claude Code. If you use **Claude Code**, it loads them
automatically when you open the repository. Use them in your work:

| When | Use |
|------|-----|
| Planning a step before coding | `planner` agent or `/plan` command |
| Writing tests first / adding tests | `tdd-guide` agent, `springboot-tdd` and `tdd-workflow` skills |
| Spring Boot / JPA / PostgreSQL code | `springboot-patterns`, `jpa-patterns`, `postgres-patterns`, `java-coding-standards` skills |
| REST API design | `api-design` skill |
| Frontend (HTML/CSS/JS) | `frontend-patterns`, `frontend-a11y` skills |
| Build or compile error | `java-build-resolver` agent or `/build-fix` command |
| After writing code (before every commit) | `java-reviewer` agent (Java) or `code-reviewer` agent, or `/code-review` command |
| Database schema or queries | `database-reviewer` agent |
| Before opening a Pull Request | `security-reviewer` agent, then `/code-review <PR number>` on the opened PR |
| Git and PRs | `git-workflow` skill, `/pr` command |

Rules for the toolkit:
- **This task file and the contract (Appendix A) win** over any ECC default. Examples: do not add
  Testcontainers, JaCoCo, Lombok or any library that is not in the contract, even if an ECC skill suggests it;
  ECC's "80% coverage" target is a goal, not a requirement.
- Do not edit files inside `.claude/`. If an ECC hook prints an error on your computer (for example a path
  like `C:\Java\Room Booking System\.claude` that does not exist on your machine), ignore it and tell the team.
- If you use another AI tool (Cursor, Copilot, ChatGPT), you can still open the matching file in
  `.claude/agents/`, `.claude/skills/` or `.claude/rules/ecc/java/` and give it to the AI as extra guidance.

---

## 1. Your role

**Aydan — REST API (controllers, DTOs, error handling, Swagger) and API tests.**
Branch: `aydan` · **Depends on:** Onur (entities, exceptions) and Huseyn (services) — needed to compile.
**Who depends on you:** Mehdi (the web page calls your API).

You build the "door" of the backend: every HTTP endpoint the frontend and Swagger/Postman use,
the JSON request/response classes, and one central place that turns exceptions into clean JSON
errors with the right status code. Controllers stay **thin**: no business rules, no queries.

## 2. Files you own (create exactly these)

| File | Content |
|------|---------|
| `src/main/java/com/roombooking/dto/CreateRoomRequest.java` | record, contract section 11 |
| `src/main/java/com/roombooking/dto/RoomResponse.java` | record + `from(Room)` |
| `src/main/java/com/roombooking/dto/CreateBookingRequest.java` | record |
| `src/main/java/com/roombooking/dto/BookingResponse.java` | record + `from(Booking, String roomName)` |
| `src/main/java/com/roombooking/dto/ErrorResponse.java` | record with nested `ErrorBody` + `of(code, message)` |
| `src/main/java/com/roombooking/controller/HealthController.java` | `GET /api/health` |
| `src/main/java/com/roombooking/controller/RoomController.java` | `/api/rooms...` |
| `src/main/java/com/roombooking/controller/BookingController.java` | `/api/bookings...` |
| `src/main/java/com/roombooking/controller/GlobalExceptionHandler.java` | `@RestControllerAdvice` |
| `src/main/java/com/roombooking/config/OpenApiConfig.java` | Swagger title/description |
| `src/test/java/com/roombooking/controller/RoomControllerTest.java` | `@WebMvcTest` |
| `src/test/java/com/roombooking/controller/BookingControllerTest.java` | `@WebMvcTest` |
| `src/test/java/com/roombooking/ApiIntegrationTest.java` | `@SpringBootTest` full flow on H2 |

You **use** (but do not write) `Room`, `Booking`, the exceptions (Onur) and `RoomService`,
`BookingService` (Huseyn) exactly as described in the contract (sections 5, 6, 10).

## 3. Before you start
Follow section 0.1 for **Onur** and **Huseyn**, each separately (one may already be in `main`, the other
only a branch, or not started yet). If a branch does not exist yet, write your code from the contract.
Your `@WebMvcTest` tests **mock the services**, so they do not need Huseyn's real logic.
Only `ApiIntegrationTest` needs everybody's real code — write it last.
Push your branch early (once the controllers compile) so Mehdi can test the UI with the real API.

## 4. Steps

### Step 1 — DTOs
Records exactly as in contract section 11.
- `RoomResponse.from(Room room)` → `new RoomResponse(room.getId(), room.getName(), room.getCapacity())`.
- `BookingResponse.from(Booking b, String roomName)` → date as `b.getBookingDate().toString()`,
  times with `TimeRules.formatTime(...)` (format `HH:mm`).
- `ErrorResponse.of(code, message)` → `new ErrorResponse(new ErrorBody(code, message))`.
Commit: `feat: add API request and response DTOs`

### Step 2 — Error handling
`GlobalExceptionHandler` with `@RestControllerAdvice` and one `@ExceptionHandler` method per case in
contract section 11, each returning `ResponseEntity<ErrorResponse>`. Use a private helper
`build(HttpStatus status, String code, String message)`. Log unexpected exceptions with
`LoggerFactory.getLogger(...)` at `error` level (never return the stack trace to the client).
Commit: `feat: add global exception handler`

### Step 3 — Controllers
- `HealthController`: `@GetMapping("/api/health")` → `Map.of("status", "ok")`.
- `RoomController` (`@RestController`, `@RequestMapping("/api/rooms")`, constructor-injected `RoomService`):
  - `POST` → `@ResponseStatus(HttpStatus.CREATED)`, body `@RequestBody CreateRoomRequest`, returns `RoomResponse`.
  - `GET` → list.
  - `GET /free` with `@RequestParam String date, String startTime, String endTime, @RequestParam(required = false) Integer minCapacity`;
    calls `roomService.findFreeRooms(new TimeSlot(date, startTime, endTime), minCapacity)`.
    **Declare `/free` before `/{id}`** (and type `{id}` as `Long`).
  - `GET /{id}`, `DELETE /{id}` → `@ResponseStatus(HttpStatus.NO_CONTENT)`.
- `BookingController` (`/api/bookings`, constructor-injected `BookingService` **and** `RoomService`):
  - `POST` → 201, calls `bookingService.createBooking(new NewBooking(r.roomId(), r.bookedBy(), r.purpose(), new TimeSlot(r.date(), r.startTime(), r.endTime())))`
    (`NewBooking` and `TimeSlot` are in `com.roombooking.service.params`, see contract section 10).
  - `GET` with optional `roomId` (Long) and `date` (String).
  - `GET /{id}`, `DELETE /{id}` → 204.
  - `roomName` for responses: private helper `roomNames()` that builds `Map<Long, String>` from
    `roomService.listRooms()` once per request; for a single booking use `roomService.getRoom(id).getName()`.
- Add `@Tag` and `@Operation(summary = "...")` (springdoc) on controllers/methods so Swagger is readable.
- `OpenApiConfig`: a `@Bean OpenAPI` with title `Room Booking System API`, version `1.0`, and a one-line description.
Check: run the app, open `http://localhost:8080/swagger-ui.html`, create a room, book it twice → second call returns 409 with the JSON error.
Commits: `feat: add room endpoints`, `feat: add booking endpoints`, `docs: configure Swagger`

### Step 4 — Controller tests (`@WebMvcTest`, services mocked)
`@WebMvcTest(RoomController.class)` / `@WebMvcTest(BookingController.class)`; mock services with
`@MockitoBean` (`org.springframework.test.context.bean.override.mockito.MockitoBean`). Use the Boot 4 imports for `@WebMvcTest` / `@AutoConfigureMockMvc` from contract section 2 notes.
Use `MockMvc` + `jsonPath`. Note: `@WebMvcTest` also loads `@RestControllerAdvice`, so error mapping is tested too.
Use `ReflectionTestUtils.setField(room, "id", 1L)` to give entities an id.

`RoomControllerTest` — at least:
- `POST /api/rooms` → 201 + JSON body
- service throws `DuplicateRoomNameException` → 409, `$.error.code == "DUPLICATE_ROOM_NAME"`
- service throws `InvalidInputException` → 400, `$.error.code == "INVALID_INPUT"`
- broken JSON body / `"capacity": "abc"` → 400 `INVALID_INPUT`
- `GET /api/rooms/free?date=...&startTime=...&endTime=...` → 200, passes params to the service (`verify`)
- `GET /api/rooms/free` without `date` → 400 `Parameter 'date' is required.`
- `GET /api/rooms/abc` → 400; `GET /api/rooms/99` with `RoomNotFoundException` → 404
- `DELETE /api/rooms/1` → 204; with `RoomInUseException` → 409

`BookingControllerTest` — at least:
- `POST /api/bookings` → 201 with `roomName`, `startTime == "10:00"` (not `"10:00:00"`)
- `BookingConflictException` → 409 with the exact message
- `GET /api/bookings?roomId=1&date=2030-12-01` → passes both filters to the service
- `GET /api/bookings/5` unknown → 404 `BOOKING_NOT_FOUND`; `DELETE /api/bookings/5` → 204
- unexpected `RuntimeException` → 500 `INTERNAL_ERROR`, message `An unexpected error occurred.`

### Step 5 — Integration test (`ApiIntegrationTest`, real services + H2, no mocks)
`@SpringBootTest` + `@AutoConfigureMockMvc`. Replace the clock so "now" is fixed:
add a nested `@TestConfiguration` class with `@Bean @Primary Clock testClock()` returning
`Clock.fixed(Instant.parse("2030-01-15T06:00:00Z"), ZoneOffset.UTC)`, and use dates like `2030-12-01`.
Annotate with `@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)` (or delete all rows in `@BeforeEach`)
so tests are independent. Full scenario tests:
- create two rooms → book room A 10:00–11:30 → booking A 11:00–12:00 is **409** → booking A 11:30–12:30 is **201**
- free rooms 10:30–11:00 returns only room B; with `minCapacity` filter works
- cancel booking → room A appears free again
- delete room with bookings → 409; after cancelling all its bookings → 204
- `GET /api/health` → `{"status":"ok"}`
Check: `mvnw test` → all green (no internet needed).
Commits: `test: add controller tests`, `test: add API integration test`

### Step 6 — Pull Request
Do section 0.1 again (`git merge origin/main`); the project must compile and `mvnw test` must pass. Push, open PR
**"REST API: controllers, DTOs, error handling, Swagger"**. Reviewer: **Mehdi**.
Put a screenshot of Swagger UI in the PR description.

## 5. Definition of Done
- [ ] All endpoints, status codes and JSON shapes match contract section 11 exactly.
- [ ] Every error returns `{"error": {"code", "message"}}` — including bad JSON and missing params.
- [ ] Swagger UI lists all endpoints with summaries.
- [ ] `mvnw test` passes (controller tests + integration test).
- [ ] PR reviewed by Mehdi and merged.

## 6. Questions you must be able to answer
1. What is REST? Why `POST` for create, `GET` for read, `DELETE` for delete?
2. Why 201, 204, 400, 404, 409, 500? Give an example request for each.
3. What do `@RestController`, `@RequestMapping`, `@GetMapping`, `@PathVariable`, `@RequestParam`, `@RequestBody`, `@ResponseStatus` do?
4. Why must `/api/rooms/free` be declared before `/api/rooms/{id}`?
5. What is a DTO and why don't we return the JPA entity directly? Why are DTOs `record`s?
6. How does `@RestControllerAdvice` work? Follow a `BookingConflictException` from the service to the JSON the browser receives.
7. Why do we never send the stack trace to the client?
8. What is the difference between `@WebMvcTest` and `@SpringBootTest`? What does `@MockitoBean` do?
9. How does Jackson turn a Java record into JSON? Why do we send times as `"HH:mm"` strings?
10. What is Swagger / OpenAPI and how does springdoc generate it?
11. Why does `BookingController` build a room-name map once instead of loading the room for each booking (N+1 problem)?
12. Why do we fix the `Clock` in the integration test?

---

# Appendix A — Technical Contract (full copy of docs/SPEC.md)


> **This file is the single source of truth for the whole team.**
> Every member (and every AI assistant) must follow the names, signatures, file paths,
> status codes and messages in this file **exactly**, so that the four parts fit together
> without changes when the branches are merged.
> If something must change, open a Pull Request that edits this file first and tell the team.
>
> **Code style:** all Java code must follow [CODE_QUALITY.md](../docs/CODE_QUALITY.md) (names, methods, comments, review checklist).

---

## 1. Product summary

A web application for booking university rooms.

Basic requirements from the course (all must work):

1. Add rooms with a name and capacity.
2. Book a room for a date and a time.
3. Do not allow two bookings at the same time in the same room.
4. Show all free rooms for a chosen time.

Extras (for extra points): list/filter bookings, cancel a booking, delete an unused room,
filter free rooms by minimum capacity, REST API + web frontend, a real online PostgreSQL
database (Neon), unit tests, layered architecture.

---

## 2. Technology stack (fixed — do not change)

| Part | Technology |
|------|------------|
| Language | **Java 27** (JDK 27) |
| Framework | **Spring Boot 4.x** — the newest stable 4.x release on start.spring.io that supports Java 27 (Spring Web MVC, Spring Data JPA, Validation) |
| Build tool | **Maven** with Maven Wrapper (`mvnw` / `mvnw.cmd`) |
| Database | **PostgreSQL** hosted online on **Neon** (https://neon.com) |
| DB driver | `org.postgresql:postgresql` |
| Tests | JUnit (Jupiter) + Mockito + AssertJ + Spring Boot Test, **H2** in-memory database (PostgreSQL mode) for tests only |
| API docs | **springdoc-openapi** (`springdoc-openapi-starter-webmvc-ui`, latest **3.x** — the line made for Spring Boot 4) → Swagger UI |
| Frontend | Plain **HTML + CSS + JavaScript** (no framework, no build step), served by Spring Boot from `src/main/resources/static/` |

Maven coordinates: `groupId = com.roombooking`, `artifactId = room-booking-system`, base package **`com.roombooking`**.

### Dependencies in `pom.xml`

| Dependency | Scope |
|------------|-------|
| `spring-boot-starter-webmvc` | compile |
| `spring-boot-starter-data-jpa` | compile |
| `spring-boot-starter-validation` | compile |
| `org.postgresql:postgresql` | runtime |
| `org.springdoc:springdoc-openapi-starter-webmvc-ui` | compile |
| `spring-boot-starter-test` | test |
| `spring-boot-starter-webmvc-test` | test |
| `spring-boot-starter-data-jpa-test` | test |
| `com.h2database:h2` | test |

No other libraries (no Lombok — we write getters/constructors ourselves so everyone understands the code).

> **Spring Boot 4 notes (important for AI assistants — many tutorials online are still Spring Boot 3):**
> - Generate `pom.xml` on start.spring.io with Java **27** selected. If a starter name generated by
>   Initializr differs from the table above, **Initializr's name wins** (it is always correct for the chosen version).
> - Boot 4 is modular: test annotations moved to new packages. Use these imports (let the IDE auto-import if a package differs):
>   `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`,
>   `org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase`,
>   `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`,
>   `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`.
> - Mock beans with `@MockitoBean` (`org.springframework.test.context.bean.override.mockito.MockitoBean`); `@MockBean` no longer exists.
> - Boot 4 uses **Jackson 3** (packages `tools.jackson.*` instead of `com.fasterxml.jackson.*`); annotations such as
>   `@JsonProperty` are still in `com.fasterxml.jackson.annotation`. We use plain records, so normally no Jackson code is needed.
> - Records, `switch` expressions, text blocks and `var` are allowed; do not use preview features (no `--enable-preview`).

### How to run (from the repository root)

1. Set three environment variables with the Neon connection (see section 7):
   `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
2. Start: Windows `mvnw.cmd spring-boot:run` · macOS/Linux `./mvnw spring-boot:run`
   (or run `RoomBookingApplication` in IntelliJ with the env variables in the Run Configuration).
3. Open:
   - Web UI: <http://localhost:8080/>
   - Swagger UI: <http://localhost:8080/swagger-ui.html>
4. Tests (no internet / Neon needed, they use H2): `mvnw.cmd test` or `./mvnw test`

---

## 3. Architecture

Layered architecture. Each layer only talks to the layer directly below it.

```
 Browser (static/: index.html, app.js, api.js, styles.css)
        │  HTTP + JSON  (fetch "/api/...")
        ▼
 Controller layer  controller/ + dto/   REST endpoints, request/response records, error → HTTP mapping
        │  calls
        ▼
 Service layer     service/             ALL business rules (validation, overlap, free rooms)
        │  calls
        ▼
 Repository layer  repository/          Spring Data JPA interfaces (queries only, no business rules)
        │
        ▼
 PostgreSQL on Neon (tests: H2 in-memory)
```

Rules:

- **Entities** (`model/`) are plain JPA classes, no business logic.
- **Repositories** contain queries only. They never throw business exceptions.
- **Services** contain every business rule and throw the exceptions from `exception/`.
  Services accept dates/times as **Strings** and parse them, so every validation message comes from one place.
- **Controllers** are thin: request DTO → call service → convert entity to response DTO. No queries, no business rules.
- **Frontend** only talks to the API with `fetch`. No business rules except "required field" checks.

---

## 4. Folder structure and file ownership

```
Room-Booking-System/
├── pom.xml                                         (Onur)
├── mvnw, mvnw.cmd, .mvn/                           (Onur)  Maven wrapper
├── .gitignore                                      (Onur)
├── .env.example                                    (Onur)  template only, no real password
├── README.md                                       (Onur)
├── src/main/java/com/roombooking/
│   ├── RoomBookingApplication.java                 (Onur)
│   ├── model/
│   │   ├── Room.java                               (Onur)
│   │   └── Booking.java                            (Onur)
│   ├── repository/
│   │   ├── RoomRepository.java                     (Onur)
│   │   └── BookingRepository.java                  (Onur)
│   ├── exception/
│   │   ├── AppException.java                       (Onur)
│   │   ├── InvalidInputException.java              (Onur)
│   │   ├── NotFoundException.java                  (Onur)
│   │   ├── ConflictException.java                  (Onur)
│   │   ├── RoomNotFoundException.java              (Onur)
│   │   ├── BookingNotFoundException.java           (Onur)
│   │   ├── DuplicateRoomNameException.java         (Onur)
│   │   ├── BookingConflictException.java           (Onur)
│   │   └── RoomInUseException.java                 (Onur)
│   ├── config/
│   │   ├── DataSeeder.java                         (Onur)
│   │   ├── ClockConfig.java                        (Huseyn)
│   │   └── OpenApiConfig.java                      (Aydan)
│   ├── service/
│   │   ├── params/
│   │   │   ├── TimeSlot.java                       (Huseyn)
│   │   │   ├── ParsedSlot.java                     (Huseyn)
│   │   │   ├── TimeRange.java                      (Huseyn)
│   │   │   └── NewBooking.java                     (Huseyn)
│   │   ├── TimeRules.java                          (Huseyn)
│   │   ├── RoomService.java                        (Huseyn)
│   │   └── BookingService.java                     (Huseyn)
│   ├── dto/
│   │   ├── CreateRoomRequest.java                  (Aydan)
│   │   ├── RoomResponse.java                       (Aydan)
│   │   ├── CreateBookingRequest.java               (Aydan)
│   │   ├── BookingResponse.java                    (Aydan)
│   │   └── ErrorResponse.java                      (Aydan)
│   └── controller/
│       ├── HealthController.java                   (Aydan)
│       ├── RoomController.java                     (Aydan)
│       ├── BookingController.java                  (Aydan)
│       └── GlobalExceptionHandler.java             (Aydan)
├── src/main/resources/
│   ├── application.properties                      (Onur)
│   ├── schema.sql                                  (Onur)
│   └── static/
│       ├── index.html                              (Mehdi)
│       ├── styles.css                              (Mehdi)
│       ├── api.js                                  (Mehdi)
│       └── app.js                                  (Mehdi)
├── src/test/resources/
│   └── application.properties                      (Onur)  H2 settings for tests
├── src/test/java/com/roombooking/
│   ├── repository/RoomRepositoryTest.java          (Onur)
│   ├── repository/BookingRepositoryTest.java       (Onur)
│   ├── service/TimeRulesTest.java                  (Huseyn)
│   ├── service/RoomServiceTest.java                (Huseyn)
│   ├── service/BookingServiceTest.java             (Huseyn)
│   ├── controller/RoomControllerTest.java          (Aydan)
│   ├── controller/BookingControllerTest.java       (Aydan)
│   └── ApiIntegrationTest.java                     (Aydan)
├── docs/
│   ├── SPEC.md                (everyone reads; change only by PR)
│   ├── REQUIREMENTS.md        (Mehdi exports it to REQUIREMENTS.pdf)
│   ├── REQUIREMENTS.pdf       (Mehdi)
├── docs/screenshots/          (Mehdi)
└── tasks/                     one task file per member
```

**Golden rule: only edit the files you own.** If you need a change in somebody else's file,
ask them. This keeps merges conflict-free.

---

## 5. Domain model — `com.roombooking.model`

### `Room.java`

```java
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false)
    private int capacity;

    protected Room() { }                                   // for JPA
    public Room(String name, int capacity) { ... }

    // getters: getId(), getName(), getCapacity()  — no setters
}
```

### `Booking.java`

```java
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;                 // plain id, no @ManyToOne (keeps the code simple)

    @Column(name = "booked_by", nullable = false, length = 100)
    private String bookedBy;

    @Column(nullable = false, length = 200)
    private String purpose;              // "" when not given

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    protected Booking() { }              // for JPA
    public Booking(Long roomId, String bookedBy, String purpose,
                   LocalDate bookingDate, LocalTime startTime, LocalTime endTime) { ... }

    // getters only: getId(), getRoomId(), getBookedBy(), getPurpose(),
    //               getBookingDate(), getStartTime(), getEndTime()
}
```

In JSON, dates are `"YYYY-MM-DD"` (e.g. `"2026-12-01"`) and times are `"HH:mm"` (e.g. `"09:30"`).

---

## 6. Exceptions — `com.roombooking.exception`

All are **unchecked** (extend `RuntimeException` through `AppException`).

```java
public abstract class AppException extends RuntimeException {
    private final String code;
    private final HttpStatus status;          // org.springframework.http.HttpStatus

    protected AppException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }
    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
```

| Class | Extends | code | status |
|-------|---------|------|--------|
| `InvalidInputException` | `AppException` | `INVALID_INPUT` | 400 BAD_REQUEST |
| `NotFoundException` (abstract) | `AppException` | – | 404 NOT_FOUND |
| `ConflictException` (abstract) | `AppException` | – | 409 CONFLICT |
| `RoomNotFoundException` | `NotFoundException` | `ROOM_NOT_FOUND` | 404 |
| `BookingNotFoundException` | `NotFoundException` | `BOOKING_NOT_FOUND` | 404 |
| `DuplicateRoomNameException` | `ConflictException` | `DUPLICATE_ROOM_NAME` | 409 |
| `BookingConflictException` | `ConflictException` | `BOOKING_CONFLICT` | 409 |
| `RoomInUseException` | `ConflictException` | `ROOM_IN_USE` | 409 |

Constructors:

```java
new InvalidInputException(String message)
new RoomNotFoundException(Long roomId)            // message: "Room with id <id> was not found."
new BookingNotFoundException(Long bookingId)      // message: "Booking with id <id> was not found."
new DuplicateRoomNameException(String name)       // message: "A room named '<name>' already exists."
new RoomInUseException(String roomName)           // message: "Room '<name>' has bookings and cannot be deleted."
new BookingConflictException(String message)      // message is built by BookingService (see section 9)
// NotFoundException / ConflictException: protected constructor (String code, String message)
```

---

## 7. Database — PostgreSQL on Neon

### 7.1 Neon setup (Onur does it once, then shares the connection privately)

1. Create a free account on https://neon.com → **New Project** (name `room-booking`, region closest to Azerbaijan, e.g. *AWS Europe (Frankfurt)*), Postgres version default.
2. Create database `roombooking` (or use the default `neondb`).
3. In **Connect**, copy the connection details. Convert them to JDBC form:
   `jdbc:postgresql://<host>/<database>?sslmode=require`
   (the host looks like `ep-xxxx-xxxx.eu-central-1.aws.neon.tech`; user and password go in separate variables).
4. Share `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` with the team **privately** (group chat), **never** in Git.
5. Optional: in Neon, create a **branch** per member (`dev-huseyn`, …) so each person can test without disturbing others' data.

### 7.2 Environment variables (each member sets them locally)

| Variable | Example |
|----------|---------|
| `DB_URL` | `jdbc:postgresql://ep-cool-name-123456.eu-central-1.aws.neon.tech/roombooking?sslmode=require` |
| `DB_USERNAME` | `neondb_owner` |
| `DB_PASSWORD` | *(secret from Neon)* |

`.env.example` in the repo shows these names with fake values. The real values go into the
IntelliJ Run Configuration → *Environment variables*, or into the terminal before running
(`set DB_URL=...` on Windows cmd, `$env:DB_URL="..."` in PowerShell, `export DB_URL=...` on macOS/Linux).

### 7.3 `src/main/resources/application.properties` (exact)

```properties
spring.application.name=room-booking-system
server.port=8080

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.hikari.maximum-pool-size=5

spring.sql.init.mode=always
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.format_sql=true

springdoc.swagger-ui.path=/swagger-ui.html
```

### 7.4 `src/test/resources/application.properties` (exact) — tests use H2, never Neon

```properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.sql.init.mode=always
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
app.seed.enabled=false
```

### 7.5 `src/main/resources/schema.sql` (exact; works on both PostgreSQL and H2)

```sql
CREATE TABLE IF NOT EXISTS rooms (
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name      VARCHAR(50)  NOT NULL UNIQUE,
    capacity  INTEGER      NOT NULL CHECK (capacity BETWEEN 1 AND 500)
);

CREATE TABLE IF NOT EXISTS bookings (
    id            BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    room_id       BIGINT       NOT NULL REFERENCES rooms (id),
    booked_by     VARCHAR(100) NOT NULL,
    purpose       VARCHAR(200) NOT NULL DEFAULT '',
    booking_date  DATE         NOT NULL,
    start_time    TIME         NOT NULL,
    end_time      TIME         NOT NULL,
    CHECK (start_time < end_time)
);

CREATE INDEX IF NOT EXISTS idx_bookings_room_date ON bookings (room_id, booking_date);
```

Case-insensitive uniqueness of room names (BR-2) is enforced in `RoomService`.

---

## 8. Repository layer (Onur) — exact interface

### `RoomRepository.java`

```java
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    List<Room> findAllByOrderByNameAsc();
}
```

### `BookingRepository.java`

```java
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByOrderByBookingDateAscStartTimeAscRoomIdAsc();
    List<Booking> findByRoomIdOrderByBookingDateAscStartTimeAsc(Long roomId);
    List<Booking> findByBookingDateOrderByStartTimeAscRoomIdAsc(LocalDate bookingDate);
    List<Booking> findByRoomIdAndBookingDateOrderByStartTimeAsc(Long roomId, LocalDate bookingDate);

    /** Bookings of this room on this date that overlap [startTime, endTime). */
    @Query("""
           SELECT b FROM Booking b
           WHERE b.roomId = :roomId
             AND b.bookingDate = :date
             AND b.startTime < :endTime
             AND b.endTime > :startTime
           ORDER BY b.startTime
           """)
    List<Booking> findOverlapping(@Param("roomId") Long roomId,
                                  @Param("date") LocalDate date,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime);

    /** Ids of rooms that have at least one booking overlapping the slot. */
    @Query("""
           SELECT DISTINCT b.roomId FROM Booking b
           WHERE b.bookingDate = :date
             AND b.startTime < :endTime
             AND b.endTime > :startTime
           """)
    Set<Long> findBookedRoomIds(@Param("date") LocalDate date,
                                @Param("startTime") LocalTime startTime,
                                @Param("endTime") LocalTime endTime);

    boolean existsByRoomId(Long roomId);
}
```

Inherited methods used by services: `save`, `findById`, `existsById`, `deleteById`, `findAll`.

---

## 9. Business rules (Huseyn implements them in the service layer)

| ID | Rule |
|----|------|
| BR-1 | Room name: trimmed (`strip()`), length 1–50 characters. `null` counts as empty. |
| BR-2 | Room names are unique, case-insensitive (`"Lab 1"` and `"lab 1"` are the same). |
| BR-3 | Room capacity: whole number from 1 to 500 (`null` is invalid). |
| BR-4 | Date format is `YYYY-MM-DD` and must be a real calendar date (`2026-02-30` is invalid). |
| BR-5 | Time format is `HH:mm`, exactly 5 characters, 24-hour (`9:00`, `24:00`, `10:00:00` are invalid). |
| BR-6 | Start time must be strictly earlier than end time (a booking is inside one day). |
| BR-7 | Bookings only inside opening hours **08:00–22:00** (start ≥ 08:00, end ≤ 22:00). |
| BR-8 | A booking cannot start in the past (date + start time must not be before "now" from the injected `Clock`). |
| BR-9 | **No double booking:** a new booking is rejected if it overlaps an existing booking of the same room on the same date. Overlap: `newStart < existingEnd AND existingStart < newEnd`. Touching ranges (10:00–11:00 and 11:00–12:00) are allowed. |
| BR-10 | `bookedBy`: trimmed, length 1–100. `null` counts as empty. |
| BR-11 | `purpose`: optional (`null` → `""`), trimmed, max 200 characters. |
| BR-12 | A free room for a slot = a room with no booking overlapping that slot (BR-9 rule). Free-room search validates BR-4…BR-7 but **not** BR-8. |
| BR-13 | Free-room search may filter by `minCapacity` (must be ≥ 1 if given); result sorted by room name. |
| BR-14 | A room that has bookings cannot be deleted. |

### Validation order in `BookingService.createBooking`

1. room exists → else `RoomNotFoundException`
2. `bookedBy` (BR-10) → `InvalidInputException`
3. `purpose` (BR-11) → `InvalidInputException`
4. date (BR-4) → `InvalidInputException`
5. times (BR-5, BR-6, BR-7) → `InvalidInputException`
6. not in the past (BR-8) → `InvalidInputException`
7. overlap (BR-9) → `BookingConflictException`
8. save and return the booking

### Exact error messages (tests and frontend rely on them)

| Situation | Exception | Message |
|-----------|-----------|---------|
| empty room name | InvalidInputException | `Room name must not be empty.` |
| name > 50 chars | InvalidInputException | `Room name must be at most 50 characters.` |
| bad capacity | InvalidInputException | `Capacity must be a whole number between 1 and 500.` |
| duplicate name | DuplicateRoomNameException | `A room named '<name>' already exists.` (trimmed name as typed by the user) |
| room not found | RoomNotFoundException | `Room with id <id> was not found.` |
| room has bookings | RoomInUseException | `Room '<name>' has bookings and cannot be deleted.` |
| bad date | InvalidInputException | `Date must be a valid date in YYYY-MM-DD format.` |
| bad time | InvalidInputException | `Time must be in HH:mm format (24-hour).` |
| start ≥ end | InvalidInputException | `Start time must be before end time.` |
| outside hours | InvalidInputException | `Bookings are allowed only between 08:00 and 22:00.` |
| in the past | InvalidInputException | `Cannot create a booking in the past.` |
| empty bookedBy | InvalidInputException | `Booked by must not be empty.` |
| bookedBy > 100 | InvalidInputException | `Booked by must be at most 100 characters.` |
| purpose > 200 | InvalidInputException | `Purpose must be at most 200 characters.` |
| bad minCapacity | InvalidInputException | `Minimum capacity must be a positive whole number.` |
| overlap | BookingConflictException | `Room '<room name>' is already booked from <HH:mm> to <HH:mm> on <YYYY-MM-DD>.` (values of the **existing** conflicting booking; if several, the earliest) |
| booking not found | BookingNotFoundException | `Booking with id <id> was not found.` |

---

## 10. Service layer (Huseyn) — exact interface

All services are Spring `@Service` classes with **constructor injection** (no `@Autowired` on fields).

### `TimeRules.java` (final utility class, static methods, no Spring, no database)

```java
public final class TimeRules {
    public static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    public static final LocalTime CLOSING_TIME = LocalTime.of(22, 0);
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private TimeRules() { }

    public static LocalDate parseDate(String value) { ... }        // BR-4, throws InvalidInputException
    public static LocalTime parseTime(String value) { ... }        // BR-5, throws InvalidInputException
    public static ParsedSlot parseSlot(TimeSlot slot) { ... }      // BR-4 to BR-7: parses date + times, validates range
    public static void validateTimeRange(LocalTime start, LocalTime end) { ... }  // BR-6, BR-7
    public static boolean intervalsOverlap(TimeRange first, TimeRange second) { ... } // BR-9
    public static String formatTime(LocalTime time) { ... }        // "HH:mm"
}
```

`parseDate` must reject impossible dates: use `LocalDate.parse(value)` (ISO, strict for day-of-month)
inside try/catch of `DateTimeParseException`; `null` → same error.
`parseTime` must first check the regex `^([01]\d|2[0-3]):[0-5]\d$`, then `LocalTime.parse(value)`.

### `ClockConfig.java`

```java
@Configuration
public class ClockConfig {
    @Bean
    public Clock clock() { return Clock.systemDefaultZone(); }
}
```

### `params/` — parameter objects (package `com.roombooking.service.params`)

Related parameters are grouped in records, so no service method has more than 3 parameters
(see CODE_QUALITY.md section 4.1). `TimeSlot` and `NewBooking` hold the raw strings from the request;
`ParsedSlot` is the checked result of `TimeRules.parseSlot` and is used inside the services.

```java
public record TimeSlot(String date, String startTime, String endTime) { }
public record NewBooking(Long roomId, String bookedBy, String purpose, TimeSlot slot) { }
public record ParsedSlot(LocalDate date, LocalTime start, LocalTime end) { }
public record TimeRange(LocalTime start, LocalTime end) { }   // used by TimeRules.intervalsOverlap
```

Controllers build them from the request, e.g.
`new NewBooking(request.roomId(), request.bookedBy(), request.purpose(), new TimeSlot(request.date(), request.startTime(), request.endTime()))`.

### `RoomService.java`

```java
@Service
public class RoomService {
    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) { ... }

    @Transactional
    public Room createRoom(String name, Integer capacity) { ... }                // BR-1, BR-2, BR-3
    @Transactional(readOnly = true)
    public List<Room> listRooms() { ... }                                        // sorted by name
    @Transactional(readOnly = true)
    public Room getRoom(Long roomId) { ... }                                     // RoomNotFoundException
    @Transactional
    public void deleteRoom(Long roomId) { ... }                                  // RoomNotFoundException, BR-14
    @Transactional(readOnly = true)
    public List<Room> findFreeRooms(TimeSlot slot, Integer minCapacity) { ... }  // BR-12, BR-13 (minCapacity may be null)
}
```

### `BookingService.java`

```java
@Service
public class BookingService {
    public BookingService(RoomRepository roomRepository, BookingRepository bookingRepository,
                          Clock clock) { ... }

    @Transactional
    public Booking createBooking(NewBooking newBooking) { ... }
    @Transactional(readOnly = true)
    public List<Booking> listBookings(Long roomId, String date) { ... }
        // both optional (null = no filter); validates date if given (BR-4);
        // RoomNotFoundException if roomId given but missing; picks the matching repository method
    @Transactional(readOnly = true)
    public Booking getBooking(Long bookingId) { ... }                            // BookingNotFoundException
    @Transactional
    public void cancelBooking(Long bookingId) { ... }                            // BookingNotFoundException
}
```

The injected `Clock` is used for "now": `LocalDateTime.now(clock)`. Tests pass `Clock.fixed(...)`.

---

## 11. REST API (Aydan) — exact contract

All endpoints are under `/api`. Request and response bodies are JSON.

| Method | Path | Body / query | Success | Errors |
|--------|------|--------------|---------|--------|
| GET | `/api/health` | – | 200 `{"status": "ok"}` | – |
| POST | `/api/rooms` | `CreateRoomRequest` | 201 `RoomResponse` | 400, 409 |
| GET | `/api/rooms` | – | 200 `RoomResponse[]` | – |
| GET | `/api/rooms/free` | query: `date`, `startTime`, `endTime` (required), `minCapacity` (optional int) | 200 `RoomResponse[]` | 400 |
| GET | `/api/rooms/{id}` | – | 200 `RoomResponse` | 404 |
| DELETE | `/api/rooms/{id}` | – | 204 (no body) | 404, 409 |
| POST | `/api/bookings` | `CreateBookingRequest` | 201 `BookingResponse` | 400, 404, 409 |
| GET | `/api/bookings` | query: `roomId` (optional long), `date` (optional) | 200 `BookingResponse[]` | 400, 404 |
| GET | `/api/bookings/{id}` | – | 200 `BookingResponse` | 404 |
| DELETE | `/api/bookings/{id}` | – | 204 (no body) | 404 |

DTOs are Java **records** in `com.roombooking.dto`:

```java
public record CreateRoomRequest(String name, Integer capacity) { }
public record RoomResponse(Long id, String name, int capacity) {
    public static RoomResponse from(Room room) { ... }
}
public record CreateBookingRequest(Long roomId, String bookedBy, String purpose,
                                   String date, String startTime, String endTime) { }
public record BookingResponse(Long id, Long roomId, String roomName, String bookedBy,
                              String purpose, String date, String startTime, String endTime) {
    public static BookingResponse from(Booking booking, String roomName) { ... } // times as "HH:mm"
}
public record ErrorResponse(ErrorBody error) {
    public record ErrorBody(String code, String message) { }
    public static ErrorResponse of(String code, String message) { ... }
}
```

`roomName` in `BookingResponse`: the controller builds a `Map<Long, String>` of room id → name
from `roomService.listRooms()` once per request (not one query per booking).

JSON examples:

```json
// RoomResponse
{ "id": 1, "name": "Lab 101", "capacity": 30 }

// CreateBookingRequest
{ "roomId": 1, "bookedBy": "Aydan", "purpose": "Study group",
  "date": "2026-12-01", "startTime": "10:00", "endTime": "11:30" }

// BookingResponse
{ "id": 7, "roomId": 1, "roomName": "Lab 101", "bookedBy": "Aydan", "purpose": "Study group",
  "date": "2026-12-01", "startTime": "10:00", "endTime": "11:30" }

// ErrorResponse (same shape for EVERY error)
{ "error": { "code": "BOOKING_CONFLICT",
             "message": "Room 'Lab 101' is already booked from 10:00 to 11:30 on 2026-12-01." } }
```

`GlobalExceptionHandler` (`@RestControllerAdvice`):

- `AppException` → status `ex.getStatus()`, body `ErrorResponse.of(ex.getCode(), ex.getMessage())`.
- `HttpMessageNotReadableException` (broken JSON / wrong type) → 400 `INVALID_INPUT`, message `Request body is invalid or has wrong types.`
- `MethodArgumentTypeMismatchException` (e.g. `/api/rooms/abc`) → 400 `INVALID_INPUT`, message `Parameter '<name>' has an invalid value.`
- `MissingServletRequestParameterException` → 400 `INVALID_INPUT`, message `Parameter '<name>' is required.`
- any other `Exception` → 500 `INTERNAL_ERROR`, message `An unexpected error occurred.` (log the real exception with SLF4J).

---

## 12. Frontend (Mehdi) — contract

- Files in `src/main/resources/static/`; Spring Boot serves `index.html` at `http://localhost:8080/`.
- Calls the API with relative URLs (`/api/rooms`), so no CORS configuration is needed.
- `api.js` wraps every endpoint in one `async` function and throws an `Error` whose `message` is the API `error.message`.
- `app.js` handles the DOM. Four sections: **Rooms**, **Book a Room**, **Free Rooms**, **Bookings**.
- Every API error is shown in a visible message box; every success shows a short confirmation.
- User text is inserted with `textContent` (never `innerHTML` with user data).

---

## 13. Coding conventions (everyone)

- Java: standard naming (`PascalCase` classes, `camelCase` methods/fields, `UPPER_SNAKE_CASE` constants), 4-space indentation.
- Constructor injection only; fields `private final`.
- Javadoc comment (`/** ... */`) on every public class and public method — one or two sentences.
- Small methods (aim for < 20 lines). No duplicated logic — use private helper methods.
- No `System.out.println` in app code (use SLF4J `Logger` if logging is needed). No commented-out code. No unused imports.
- No Lombok.
- JavaScript: `const`/`let` (no `var`), `camelCase`, `async/await`, `addEventListener` (no inline `onclick`).
- Tests: JUnit 5, method names describe behaviour, e.g. `createBooking_rejectsOverlappingTime()`, use `@DisplayName` for readable output.
- Never commit: `target/`, `.idea/`, `*.iml`, `.vscode/`, `.env`, real Neon passwords.

## 14. Git workflow (everyone)

1. `git checkout main` and `git pull`
2. `git checkout -b <YourName>` (branch names: `Onur`, `huseyn`, `aydan`, `mehdi`)
3. Small commits with clear messages: `feat: ...`, `fix: ...`, `test: ...`, `docs: ...`, `refactor: ...`, `chore: ...`
4. `git push -u origin <YourName>`
5. Open a Pull Request into `main` on GitHub. Ask **one other member** to review it.
6. The reviewer reads the code, runs `mvnw test`, leaves at least one comment, approves.
7. Merge; then everyone updates their branch: `git fetch origin`, `git merge origin/main`.
8. There is no fixed merge order: a PR is merged when, after merging `origin/main` into it, the project compiles and `mvnw test` passes.

Each member must have **at least 3 commits and at least 1 merged Pull Request** under their own GitHub account.
