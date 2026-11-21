# Room Booking System — Business Requirements Document

| | |
|---|---|
| **Course** | IT 371 — Mini Project (Project 6) |
| **Team** | Onur, Huseyn, Aydan, Mehdi |
| **Repository** | https://github.com/hvs3yn/Room-Booking-System |
| **Version** | 1.0 |

---

## 1. Introduction

### 1.1 Purpose
This document describes the business needs, functional requirements and user stories for the
**Room Booking System**: a small application that lets university staff and students add rooms,
book them for a date and time, and quickly find rooms that are free.

### 1.2 Problem statement
Today rooms are booked informally (messages, paper lists). This causes double bookings, wasted
time searching for an empty room, and no clear overview of who uses which room and when.

### 1.3 Business objectives
| ID | Objective |
|----|-----------|
| BO-1 | Eliminate double bookings of the same room. |
| BO-2 | Let a user find a free room for a given time in a few seconds. |
| BO-3 | Keep a single, persistent list of rooms and bookings. |
| BO-4 | Keep the system simple enough to use without training. |

---

## 2. Scope

### 2.1 In scope
- Managing rooms (add, list, view, delete when unused).
- Creating bookings for a room, date and time range.
- Preventing overlapping bookings in the same room.
- Searching free rooms for a chosen date and time range (optionally by minimum capacity).
- Listing, filtering and cancelling bookings.
- Persistent storage in an online PostgreSQL database hosted on Neon.
- A web interface and a REST API with interactive documentation (Swagger).

### 2.2 Out of scope
- User accounts, login and permissions.
- Recurring bookings (for example "every Monday").
- Email or push notifications.
- Payments and public deployment of the web server (optional extra, not required).

---

## 3. Stakeholders and users

| Stakeholder | Interest |
|-------------|----------|
| **Room administrator** (faculty staff) | Adds and removes rooms, keeps the room list correct. |
| **Room user** (student or teacher) | Finds a free room and books it; cancels bookings that are no longer needed. |
| **Course teacher** | Evaluates the working software, code quality and teamwork. |
| **Development team** | Onur, Huseyn, Aydan, Mehdi. |

---

## 4. Assumptions and constraints

- One university, one time zone; the server's local time is used as "now".
- A booking is always inside a single day.
- Rooms can be booked only during opening hours **08:00–22:00**.
- No authentication: anyone using the app may book or cancel (accepted for this project).
- Technology: Java 27, Spring Boot 4, PostgreSQL on Neon, HTML/CSS/JavaScript (see `docs/SPEC.md`).
- The application needs an internet connection to reach the Neon database; automated tests run offline with an in-memory H2 database.
- Delivery within one month by a team of four, using Git branches and pull requests.

### 4.1 Development approach and AI tooling

- **Spec-driven development:** this document (what to build) and `docs/SPEC.md` (how to build it)
  were written first; each member then builds their part from a task file in `tasks/`.
- **AI toolkit in the repository:** the `.claude/` folder contains *Everything Claude Code (ECC)*,
  a set of AI agents, skills, commands and rules for Claude Code. The team uses it to write,
  test and review code, for example the `java-reviewer` and `code-reviewer` agents for code review,
  `java-build-resolver` for build errors, `tdd-guide` and the `springboot-tdd` skill for tests,
  `database-reviewer` for the PostgreSQL schema and `security-reviewer` before each Pull Request.
- Every member must understand and be able to explain all code, including code written with AI help.

---

## 5. Functional requirements

Priority: **M** = Must (course basic requirement), **S** = Should (extra), **C** = Could.

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-1 | The system shall allow a user to add a room with a **name** and a **capacity**. | M |
| FR-2 | The system shall reject a room whose name is empty, longer than 50 characters, or already used (case-insensitive). | M |
| FR-3 | The system shall reject a capacity that is not a whole number between 1 and 500. | M |
| FR-4 | The system shall display the list of all rooms sorted by name. | M |
| FR-5 | The system shall allow a user to book a room by giving the room, their name, an optional purpose, a date, a start time and an end time. | M |
| FR-6 | The system shall reject a booking with an invalid date or time, a start time not before the end time, a time outside 08:00–22:00, or a start in the past. | M |
| FR-7 | The system shall **not allow two bookings in the same room whose times overlap** on the same date, and shall tell the user which existing booking causes the conflict. Back-to-back bookings (e.g. 10:00–11:00 and 11:00–12:00) are allowed. | M |
| FR-8 | The system shall show **all rooms that are free** for a chosen date, start time and end time. | M |
| FR-9 | The free-room search shall optionally filter by a minimum capacity. | S |
| FR-10 | The system shall list bookings, optionally filtered by room and/or date, sorted by date and start time. | S |
| FR-11 | The system shall allow a user to cancel (delete) a booking. | S |
| FR-12 | The system shall allow deleting a room only when it has no bookings. | S |
| FR-13 | All rooms and bookings shall be stored persistently and remain available after the application restarts. | M |
| FR-14 | The system shall provide a REST API for all functions above, documented in Swagger UI. | S |
| FR-15 | The system shall show clear success and error messages for every user action. | S |

---

## 6. Business rules

| ID | Rule |
|----|------|
| BR-1 | Room name is trimmed and must be 1–50 characters long. |
| BR-2 | Room names are unique, case-insensitive. |
| BR-3 | Capacity is a whole number from 1 to 500. |
| BR-4 | Dates use the format YYYY-MM-DD and must be real calendar dates. |
| BR-5 | Times use the 24-hour format HH:MM. |
| BR-6 | Start time must be earlier than end time. |
| BR-7 | Bookings are allowed only between 08:00 and 22:00. |
| BR-8 | A booking cannot start in the past. |
| BR-9 | Two bookings of the same room on the same date may not overlap (`newStart < existingEnd` and `existingStart < newEnd`). |
| BR-10 | "Booked by" is required, max 100 characters. |
| BR-11 | Purpose is optional, max 200 characters. |
| BR-12 | A room is free for a slot when none of its bookings overlaps the slot. |
| BR-13 | Free rooms can be filtered by minimum capacity; results are sorted by name. |
| BR-14 | A room with bookings cannot be deleted. |

---

## 7. User stories and acceptance criteria

### US-1 — Add a room (FR-1, FR-2, FR-3)
**As a** room administrator, **I want to** add a room with a name and capacity, **so that** people can book it.

Acceptance criteria:
1. **Given** the name "Lab 101" is not used, **when** I add "Lab 101" with capacity 30, **then** the room is saved and appears in the room list.
2. **Given** a room "Lab 101" exists, **when** I add "lab 101", **then** I see the error *"A room named 'lab 101' already exists."* and no room is added.
3. **When** I add a room with an empty name, **then** I see *"Room name must not be empty."*
4. **When** I add a room with capacity 0, 501 or "abc", **then** I see *"Capacity must be a whole number between 1 and 500."*

### US-2 — View rooms (FR-4)
**As a** room user, **I want to** see all rooms with their capacity, **so that** I know which rooms exist.

Acceptance criteria:
1. **Given** rooms "B-201" and "A-101" exist, **when** I open the Rooms section, **then** I see both rooms sorted by name ("A-101" first) with their capacities.
2. **Given** no rooms exist, **when** I open the Rooms section, **then** I see the message "No rooms yet."

### US-3 — Book a room (FR-5, FR-6)
**As a** room user, **I want to** book a room for a date and time, **so that** the room is reserved for me.

Acceptance criteria:
1. **Given** room "Lab 101" is free on 2026-12-01 from 10:00 to 11:30, **when** I book it with my name, **then** the booking is saved and shown in the bookings list.
2. **When** the start time is 12:00 and the end time is 11:00, **then** I see *"Start time must be before end time."*
3. **When** I book from 07:00 to 09:00, **then** I see *"Bookings are allowed only between 08:00 and 22:00."*
4. **When** I book a date/time that has already passed, **then** I see *"Cannot create a booking in the past."*
5. **When** my name is empty, **then** I see *"Booked by must not be empty."*
6. **When** the room does not exist, **then** I see *"Room with id <id> was not found."*

### US-4 — Prevent double booking (FR-7)
**As a** room user, **I want** the system to stop overlapping bookings, **so that** two groups never arrive at the same room at the same time.

Acceptance criteria:
1. **Given** "Lab 101" is booked on 2026-12-01 from 10:00 to 11:30, **when** someone books "Lab 101" on 2026-12-01 from 11:00 to 12:00, **then** the booking is rejected with *"Room 'Lab 101' is already booked from 10:00 to 11:30 on 2026-12-01."*
2. **Given** the same existing booking, **when** someone books 11:30–12:30 in "Lab 101", **then** the booking is accepted (back-to-back is allowed).
3. **Given** the same existing booking, **when** someone books 10:00–11:30 in a **different** room, **then** the booking is accepted.
4. **Given** the same existing booking, **when** someone books 10:00–11:30 in "Lab 101" on **another date**, **then** the booking is accepted.
5. **Given** the same existing booking, **when** someone books 09:00–13:00 (fully covering it), **then** the booking is rejected.

### US-5 — Find free rooms (FR-8, FR-9)
**As a** room user, **I want to** see all rooms that are free for a chosen time, **so that** I can quickly pick one.

Acceptance criteria:
1. **Given** rooms "A" and "B" exist and only "A" is booked 10:00–11:00 on 2026-12-01, **when** I search 2026-12-01 10:30–11:30, **then** only "B" is shown.
2. **When** I search 2026-12-01 11:00–12:00 in the same situation, **then** both "A" and "B" are shown.
3. **Given** "A" has capacity 10 and "B" capacity 40, **when** I search with minimum capacity 20, **then** only "B" is shown.
4. **When** no room is free, **then** I see the message "No free rooms for this time."
5. **When** I search with an invalid time range, **then** I see the same validation messages as in US-3.
6. **When** I click "Book" next to a free room, **then** the booking form opens pre-filled with that room, date and times.

### US-6 — View and filter bookings (FR-10)
**As a** room user, **I want to** see bookings filtered by room and/or date, **so that** I know when a room is busy.

Acceptance criteria:
1. **When** I open the Bookings section, **then** I see all bookings sorted by date and start time, with room name, person, purpose and times.
2. **When** I choose a room and/or a date filter, **then** only matching bookings are shown.
3. **When** no booking matches, **then** I see the message "No bookings found."

### US-7 — Cancel a booking (FR-11)
**As a** room user, **I want to** cancel a booking, **so that** the room becomes free for others.

Acceptance criteria:
1. **Given** a booking exists, **when** I click "Cancel" and confirm, **then** the booking is removed and the room appears as free for that time.
2. **When** I try to cancel a booking that no longer exists, **then** I see *"Booking with id <id> was not found."*

### US-8 — Delete a room (FR-12)
**As a** room administrator, **I want to** delete a room that is no longer used, **so that** the list stays correct.

Acceptance criteria:
1. **Given** a room without bookings, **when** I delete it and confirm, **then** it disappears from the list.
2. **Given** a room with bookings, **when** I try to delete it, **then** I see *"Room '<name>' has bookings and cannot be deleted."*

### US-9 — Data is kept after restart (FR-13)
**As a** room administrator, **I want** the data to be saved, **so that** nothing is lost when the app restarts.

Acceptance criteria:
1. **Given** I added rooms and bookings, **when** the application is stopped and started again, **then** all rooms and bookings are still there.

### US-10 — Use the API (FR-14)
**As a** developer or tester, **I want** a documented REST API, **so that** I can test the system with Swagger or Postman.

Acceptance criteria:
1. **When** I open `/swagger-ui.html`, **then** I see all endpoints from `docs/SPEC.md` section 11 and can call them.
2. **When** a request fails, **then** the response has the correct HTTP status (400/404/409) and the body `{"error": {"code": ..., "message": ...}}`.

---

## 8. Non-functional requirements

| ID | Requirement |
|----|-------------|
| NFR-1 | **Usability:** every main task (add room, book, find free rooms) can be done from one page in at most 3 steps. |
| NFR-2 | **Performance:** every API request answers in under 1 second for up to 1,000 bookings on a normal laptop. |
| NFR-3 | **Reliability:** invalid input never crashes the app; the user always gets a readable message. |
| NFR-4 | **Data integrity:** overlap is checked on the server, not only in the browser. |
| NFR-5 | **Maintainability:** layered architecture (API → Service → Repository), type hints, small functions. |
| NFR-6 | **Testability:** business rules are covered by automated unit tests (JUnit 5 + Mockito). |
| NFR-7 | **Security:** all database access uses Spring Data JPA with bound parameters (no SQL injection); database credentials are kept in environment variables, never in Git; user text is shown with `textContent` (no HTML injection). |
| NFR-8 | **Portability:** runs on Windows, macOS and Linux with Java 27 and a modern browser. |

---

## 9. Data requirements

**Room** (table `rooms`): `id` (bigint, auto), `name` (varchar 50, unique), `capacity` (integer, 1–500).

**Booking** (table `bookings`): `id` (bigint, auto), `room_id` (reference to Room), `booked_by` (varchar 100),
`purpose` (varchar 200), `booking_date` (date), `start_time` (time), `end_time` (time).

Relationship: one Room has many Bookings; a Booking belongs to exactly one Room.

---

## 10. Traceability matrix

| Requirement | User story | Implemented in | Tested in | Owner |
|-------------|-----------|----------------|-----------|-------|
| FR-1..FR-4 | US-1, US-2 | `RoomService`, `RoomController`, Rooms UI | `RoomServiceTest`, `RoomControllerTest` | Huseyn / Aydan / Mehdi |
| FR-5, FR-6 | US-3 | `BookingService`, `TimeRules`, `BookingController`, Book UI | `BookingServiceTest`, `TimeRulesTest` | Huseyn / Aydan / Mehdi |
| FR-7 | US-4 | `BookingService`, `BookingRepository.findOverlapping` | `BookingServiceTest`, `BookingRepositoryTest`, `ApiIntegrationTest` | Huseyn / Onur |
| FR-8, FR-9 | US-5 | `RoomService.findFreeRooms`, `BookingRepository.findBookedRoomIds`, Free Rooms UI | `RoomServiceTest`, `ApiIntegrationTest` | Huseyn / Onur / Mehdi |
| FR-10, FR-11 | US-6, US-7 | `BookingService`, `BookingController`, Bookings UI | `BookingServiceTest`, `BookingControllerTest` | Huseyn / Aydan / Mehdi |
| FR-12 | US-8 | `RoomService.deleteRoom` | `RoomServiceTest` | Huseyn |
| FR-13 | US-9 | Neon PostgreSQL, `schema.sql`, repositories | `RoomRepositoryTest`, `BookingRepositoryTest` | Onur |
| FR-14, FR-15 | US-10 | `controller/*`, `GlobalExceptionHandler`, frontend | `ApiIntegrationTest` | Aydan / Mehdi |

---

## 11. Glossary

| Term | Meaning |
|------|---------|
| Room | A physical university room that can be booked. |
| Capacity | Maximum number of people a room holds. |
| Booking | A reservation of one room for one time range on one date. |
| Slot | A date + start time + end time chosen by the user. |
| Overlap | Two time ranges that share at least one minute. |
| Free room | A room with no booking overlapping the chosen slot. |
| API | Application Programming Interface — the `/api/...` HTTP endpoints. |
