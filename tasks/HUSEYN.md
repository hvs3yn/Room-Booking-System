# Task File — Huseyn (Business logic)

> **How to use this file (for Huseyn):**
> Give this whole file to your AI coding assistant. No extra prompt is needed.
> - Claude Code / Cursor / Copilot (repository open): write `Do the task in tasks/HUSEYN.md`
> - Chat AI (ChatGPT, Claude.ai…): paste the whole file, then create the files it gives you.
>
> After the AI finishes, **read and understand every line**. The teacher will ask you questions.

---

## 0. Instructions for the AI assistant (read first)

You are a senior Java engineer (up to date with Java 27 and Spring Boot 4 — do not use Spring Boot 3 APIs or imports) and a patient teacher helping **Huseyn**, a university student,
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
6. Start by giving the Git commands to switch to the branch `huseyn` (create it from an
   up-to-date `main` if it does not exist).
7. Never write a real database password into any file. Neon credentials only come from the
   environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
8. Finish by giving the commands to push the branch and the text for the Pull Request
   (title + description: what was done, how it was tested).
9. At the end, write **"Explanation for Huseyn"**: explain every file you wrote in simple
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

## 1. Your role

**Huseyn — Business logic (service layer) and the main unit tests.**
Branch: `huseyn` · **Depends on:** Onur (entities, repositories, exceptions — needed to compile).
**Who depends on you:** Aydan (controllers call your services).

You write the "brain" of the application: every validation rule, the **no double booking** rule,
and the **free rooms** search. You also write the most important unit tests of the project.
Your code must not know anything about HTTP or JSON — only Java objects, repositories and exceptions.

## 2. Files you own (create exactly these)

| File | Content |
|------|---------|
| `src/main/java/com/roombooking/service/TimeRules.java` | Contract section 10. |
| `src/main/java/com/roombooking/service/RoomService.java` | Contract section 10. |
| `src/main/java/com/roombooking/service/BookingService.java` | Contract section 10. |
| `src/main/java/com/roombooking/config/ClockConfig.java` | Contract section 10. |
| `src/test/java/com/roombooking/service/TimeRulesTest.java` | Step 4. |
| `src/test/java/com/roombooking/service/RoomServiceTest.java` | Step 4. |
| `src/test/java/com/roombooking/service/BookingServiceTest.java` | Step 4. |

You **use** (but do not write) `Room`, `Booking`, `RoomRepository`, `BookingRepository` and all
exceptions — they are written by Onur exactly as in the contract (sections 5, 6, 8).

## 3. Before you start
Follow section 0.1 for **Onur**: if Onur's work is in `main`, merge `origin/main`; if only the `Onur` branch exists,
merge `origin/Onur`; if nothing exists yet, write your code from the contract and compile later.
Your unit tests use **Mockito mocks** of the repositories, so they need no database and no real repository code.
Push your branch early (once `RoomService` compiles) so Aydan can use it.

## 4. Steps

### Step 1 — `TimeRules` + `ClockConfig`
Implement exactly as in contract section 10 with the messages from section 9:
- `parseDate(String)`: `null`/blank/invalid → `InvalidInputException("Date must be a valid date in YYYY-MM-DD format.")`.
  Use `LocalDate.parse` and catch `DateTimeParseException`. Also check the regex `^\d{4}-\d{2}-\d{2}$` first.
- `parseTime(String)`: check regex `^([01]\d|2[0-3]):[0-5]\d$` first, then `LocalTime.parse`;
  error → `"Time must be in HH:mm format (24-hour)."`.
- `validateTimeRange(start, end)`: `!start.isBefore(end)` → `"Start time must be before end time."`;
  `start.isBefore(OPENING_TIME) || end.isAfter(CLOSING_TIME)` → `"Bookings are allowed only between 08:00 and 22:00."`.
- `intervalsOverlap(a1, a2, b1, b2)`: `a1.isBefore(b2) && b1.isBefore(a2)`.
- `formatTime(LocalTime)`: `TIME_FORMAT.format(time)`.
Commit: `feat: add time validation rules and clock bean`

### Step 2 — `RoomService`
- `createRoom(name, capacity)`: `validateName` (BR-1) → `validateCapacity` (BR-3) →
  `existsByNameIgnoreCase(trimmed)` → `DuplicateRoomNameException(trimmed)` (BR-2) → `save(new Room(trimmed, capacity))`.
- `listRooms()`: `findAllByOrderByNameAsc()`.
- `getRoom(id)`: `findById(id).orElseThrow(() -> new RoomNotFoundException(id))`.
- `deleteRoom(id)`: `getRoom(id)`; if `bookingRepository.existsByRoomId(id)` → `RoomInUseException(room.getName())`; else `deleteById(id)`.
- `findFreeRooms(date, startTime, endTime, minCapacity)`: parse date and times with `TimeRules`,
  `validateTimeRange`; if `minCapacity != null && minCapacity < 1` → `"Minimum capacity must be a positive whole number."`;
  `bookedIds = bookingRepository.findBookedRoomIds(...)`; return `findAllByOrderByNameAsc()` filtered with a stream:
  not in `bookedIds` and (`minCapacity == null || capacity >= minCapacity`).
- Use private helpers `validateName` and `validateCapacity`. No method longer than ~20 lines.
Commit: `feat: add room service`

### Step 3 — `BookingService`
- Constructor takes `RoomRepository`, `BookingRepository`, `Clock`.
- `createBooking(roomId, bookedBy, date, startTime, endTime, purpose)` — follow the **validation order**
  in contract section 9 exactly. Private helpers: `findRoomOrThrow`, `validateBookedBy`, `normalizePurpose`,
  `ensureNotInPast`, `ensureNoOverlap`.
  - "now" = `LocalDateTime.now(clock)`; reject if `LocalDateTime.of(date, start).isBefore(now)`.
  - `ensureNoOverlap`: `bookingRepository.findOverlapping(roomId, date, start, end)`; if not empty, take the
    first (it is sorted by start time) and throw
    `new BookingConflictException("Room '" + room.getName() + "' is already booked from " + formatTime(existing.getStartTime()) + " to " + formatTime(existing.getEndTime()) + " on " + existing.getBookingDate() + ".")`.
  - Save `new Booking(roomId, bookedBy.strip(), purpose, date, start, end)` and return the saved object.
- `listBookings(roomId, date)`: if `roomId != null` check the room exists (`RoomNotFoundException`);
  if `date != null` parse it; then choose one of the four repository methods (none / room / date / room+date).
- `getBooking(id)`: `orElseThrow(() -> new BookingNotFoundException(id))`.
- `cancelBooking(id)`: `getBooking(id)` then `deleteById(id)`.
- Annotate with `@Transactional` / `@Transactional(readOnly = true)` as in the contract.
Commit: `feat: add booking service with double-booking rule`

### Step 4 — Unit tests (JUnit 5 + Mockito, no Spring context, no database)
Use `@ExtendWith(MockitoExtension.class)`, `@Mock RoomRepository`, `@Mock BookingRepository`, and
create the service in `@BeforeEach` with `Clock.fixed(Instant.parse("2030-01-15T09:00:00Z"), ZoneOffset.UTC)`
(so "now" is 2030-01-15 09:00 and the tests never depend on today's date).
Use AssertJ (`assertThat`, `assertThatThrownBy(...).isInstanceOf(...).hasMessage(...)`) — it comes with `spring-boot-starter-test`. You may use Java 27 features such as records and `switch` expressions where they make code clearer.
For saved entities use `when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0))`.
To give a `Room` an id in tests, use `ReflectionTestUtils.setField(room, "id", 1L)`.
Add `@DisplayName` with a readable sentence on every test.

`TimeRulesTest` (use `@ParameterizedTest` + `@ValueSource` / `@CsvSource` where it fits):
- valid date parses; invalid dates `2030-02-30`, `2030-13-01`, `30-01-2030`, `""`, `null` rejected
- valid times `08:00`, `21:59`; invalid `9:00`, `24:00`, `10:60`, `10:00:00`, `abc` rejected
- start == end and start > end rejected; `07:59` start and `22:01` end rejected; `08:00–22:00` accepted
- `intervalsOverlap`: partial overlap, inside, around, identical → true; touching (10–11 & 11–12) and separate → false

`RoomServiceTest`:
- creates room with trimmed name; empty / blank / null name rejected; 51-char name rejected
- capacity `0`, `501`, `null` rejected; `1` and `500` accepted
- duplicate name (different case) → `DuplicateRoomNameException` with exact message; `save` never called (`verify(..., never())`)
- `getRoom` unknown id → `RoomNotFoundException`
- `deleteRoom` with bookings → `RoomInUseException`; without bookings → `deleteById` called
- `findFreeRooms` excludes booked room ids; filters by `minCapacity`; `minCapacity = 0` rejected; invalid time range rejected

`BookingServiceTest`:
- happy path saves a booking with trimmed values and `purpose = ""` when `null`
- unknown room → `RoomNotFoundException` (and nothing else is checked)
- empty `bookedBy`, 101-char `bookedBy`, 201-char `purpose` rejected
- invalid date / time / reversed range / outside hours rejected with exact messages
- booking in the past (`2030-01-15` at `08:00`, now is `09:00`) rejected; `2030-01-15` at `10:00` accepted
- overlap → `BookingConflictException` with the exact message containing the **existing** booking's times
- `findOverlapping` returns empty → booking accepted (covers touching ranges at the service level)
- `listBookings` calls the correct repository method for each of the 4 filter combinations; unknown room → exception
- `getBooking` / `cancelBooking` unknown id → `BookingNotFoundException`; existing → `deleteById` called

Check: `mvnw test` → all green. Aim for **40+ test cases** (parameterized cases count).
Commits: `test: add time rules tests`, `test: add room service tests`, `test: add booking service tests`

### Step 5 — Pull Request
Push and open PR **"Business logic: validation, double-booking rule, free-room search"** into `main`
(first do section 0.1 again: `git merge origin/main`; the project must compile and `mvnw test` must pass). Reviewer: **Onur**.
In the PR description, list which business rule (BR-1…BR-14) is covered by which test.

## 5. Definition of Done
- [ ] All service methods match the contract signatures exactly.
- [ ] Every business rule BR-1…BR-14 is implemented and has at least one test.
- [ ] Every error message matches contract section 9 character by character.
- [ ] `mvnw test` passes; service tests run without a database.
- [ ] PR reviewed by Onur and merged.

## 6. Questions you must be able to answer
1. Why are business rules in the service layer and not in the controller or the repository?
2. Explain the overlap formula `newStart < existingEnd AND existingStart < newEnd` with an example. Why do touching bookings not overlap?
3. Walk through `createBooking` step by step. Why does the validation order matter?
4. How does `findFreeRooms` work? Why do we fetch booked room ids once instead of checking each room separately?
5. Why do we inject a `Clock` instead of calling `LocalDateTime.now()` directly? How do the tests use `Clock.fixed`?
6. What is a mock? What do `@Mock`, `when(...).thenReturn(...)` and `verify(..., never())` do?
7. What is the difference between a unit test and an integration test? Which ones did you write?
8. What does `@Transactional` do? Why `readOnly = true` for read methods?
9. What is constructor injection and why is it better than `@Autowired` on fields?
10. Why do we validate `"24:00"`, `"9:00"` and `"2030-02-30"` with a regex **and** `LocalDate/LocalTime.parse`?
11. What is a parameterized test and why is it useful here?
12. What would happen if two users booked the same room at the exact same millisecond? (Hint: the check runs inside a transaction; for full safety a database constraint or lock is needed — explain the idea.)

---

# Appendix A — Technical Contract (full copy of docs/SPEC.md)


> **This file is the single source of truth for the whole team.**
> Every member (and every AI assistant) must follow the names, signatures, file paths,
> status codes and messages in this file **exactly**, so that the four parts fit together
> without changes when the branches are merged.
> If something must change, open a Pull Request that edits this file first and tell the team.

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
    public static void validateTimeRange(LocalTime start, LocalTime end) { ... }  // BR-6, BR-7
    public static boolean intervalsOverlap(LocalTime startA, LocalTime endA,
                                           LocalTime startB, LocalTime endB) { ... } // BR-9
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
    public List<Room> findFreeRooms(String date, String startTime, String endTime,
                                    Integer minCapacity) { ... }                 // BR-12, BR-13 (minCapacity may be null)
}
```

### `BookingService.java`

```java
@Service
public class BookingService {
    public BookingService(RoomRepository roomRepository, BookingRepository bookingRepository,
                          Clock clock) { ... }

    @Transactional
    public Booking createBooking(Long roomId, String bookedBy, String date,
                                 String startTime, String endTime, String purpose) { ... }
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
