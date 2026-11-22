# Code Quality Rules (Java)

> Every team member follows these rules. They work together with [SPEC.md](SPEC.md):
> SPEC.md says **what** we build, this file says **how the code must look**.
> Before you open a Pull Request, check your code with the checklist in section 8.

---

## 1. Why code quality?

A program has two readers: the computer and the human. The computer reads any code that
compiles. The human is different. We spend about **ten times more time reading code than
writing it**: to fix a bug, to add a feature, or to understand what a teammate wrote.

> 💡 Write code as if the person who reads it later is a tired programmer who knows where
> you live. Very often that person is **you, six months later**.

---

## 2. Coding standard

All code must look like **one person** wrote it.

| What | Rule | Example |
|---|---|---|
| Class, interface, record, enum | `PascalCase`, noun | `Booking`, `RoomService`, `BookingResponse` |
| Method | `camelCase`, verb | `createBooking()`, `findFreeRooms()` |
| Variable, field, parameter | `camelCase`, noun | `startTime`, `freeRooms` |
| Constant (`static final`) | `UPPER_SNAKE_CASE` | `MAX_BOOKING_HOURS` |
| Package | lowercase, no `_` | `com.roombooking.service` |
| Boolean | reads like a question | `isFree`, `hasBookings`, `canDelete` |
| Indentation | **4 spaces**, no tabs | |
| Line length | at most **120** characters | |
| Braces | `{` at the end of the line, **always** use braces (also for one-line `if`) | `if (room == null) {` |
| Files | one public top-level class per file | `Room.java` contains `Room` |
| Imports | no wildcard imports (`import java.util.*;`) | `import java.util.List;` |

Member order inside a class:

1. constants (`static final`)
2. fields
3. constructors
4. public methods
5. private helper methods

**Let the tools do the work:** in IntelliJ IDEA press **Ctrl+Alt+L** (reformat) and
**Ctrl+Alt+O** (optimize imports) before every commit.

---

## 3. Good names

Names are the cheapest way to make code better. A good name tells the reader what a value
is, so the reader does not need to read the code behind it.

| ❌ Bad | ✅ Good | Why |
|---|---|---|
| `d` | `bookingDate` | says the meaning |
| `list2` | `conflictingBookings` | says what is inside |
| `calc()` | `countFreeRooms()` | says what it returns |
| `flag` | `isCancelled` | a boolean reads like a question |
| `data`, `temp`, `obj` | `room`, `request` | too general |
| `RoomManagerHelperUtil` | `RoomService` | no empty words |

- **Methods are verbs** (`cancelBooking`), **classes and variables are nouns** (`Room`, `capacity`).
- **One word for one idea.** In this project we use `find...` for reading data
  (`findById`, `findFreeRooms`). Do not mix `get`, `fetch`, `retrieve` and `load` for the same thing.
- **Length follows the scope.** `i` is fine in a 3-line loop. A field needs a full name.
- **No abbreviations** except well-known ones (`id`, `dto`, `url`).

> ✅ **Rule:** if a name needs a comment to explain it, **change the name** instead.

---

## 4. Good methods

### 4.1 Rules

| Rule | Meaning |
|---|---|
| **One job** | If you describe the method with "**and**", split it. |
| **Small** | Usually under **20 lines**, never over 50. It must fit on one screen. |
| **Few parameters** | 0–3 is good. 4 or more → put them in a record (e.g. `CreateBookingRequest`). |
| **No surprises** | `findRoom()` must not change data. A hidden change is a *side effect*. |
| **One level of detail** | Do not mix the big idea with small details in one method. |
| **Never return `null` for lists** | Return an empty list: `List.of()`. |

### 4.2 Before and after

❌ Hard to read: short names, magic numbers, three jobs in one method.

```java
public void p(List<Booking> l, int c) {
    int s = 0;
    for (int i = 0; i < l.size(); i++) {
        if (l.get(i).getRoom().getCapacity() >= c) {
            if (Duration.between(l.get(i).getStartTime(), l.get(i).getEndTime()).toMinutes() > 240) {
                System.out.println("too long");
            } else {
                System.out.println(l.get(i).getRoom().getName());
                s++;
            }
        }
    }
    System.out.println("Total: " + s);
}
```

✅ Clean: every name says its meaning, each method has one job, the number has a name.

```java
private static final Duration MAX_BOOKING_LENGTH = Duration.ofHours(4);

public List<Booking> findValidBookings(List<Booking> bookings, int minCapacity) {
    return bookings.stream()
            .filter(booking -> hasEnoughSeats(booking, minCapacity))
            .filter(this::isAllowedLength)
            .toList();
}

private boolean hasEnoughSeats(Booking booking, int minCapacity) {
    return booking.getRoom().getCapacity() >= minCapacity;
}

private boolean isAllowedLength(Booking booking) {
    Duration length = Duration.between(booking.getStartTime(), booking.getEndTime());
    return length.compareTo(MAX_BOOKING_LENGTH) <= 0;
}
```

Printing is not the job of a service. The controller returns the data, and the frontend shows it.

### 4.3 Early return (guard clauses)

Deep nesting (an `if` inside an `if` inside an `if`) is hard to follow.
**Check the problems first and leave the method at once.** Maximum nesting: **3 levels**.

❌ Nested:

```java
public boolean canBook(Room room, LocalTime start, LocalTime end) {
    if (room != null) {
        if (start != null && end != null) {
            if (start.isBefore(end)) {
                if (room.isActive()) {
                    return true;
                }
            }
        }
    }
    return false;
}
```

✅ Guard clauses:

```java
public boolean canBook(Room room, LocalTime start, LocalTime end) {
    if (room == null || start == null || end == null) {
        return false;
    }
    if (!start.isBefore(end)) {
        return false;
    }
    return room.isActive();
}
```

### 4.4 Magic numbers and strings

A **magic number** is a number in the code without a name. What does `240` mean? What does `8` mean?

❌ `if (start.getHour() < 8 || end.getHour() > 22)`

✅
```java
private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
private static final LocalTime CLOSING_TIME = LocalTime.of(22, 0);

if (start.isBefore(OPENING_TIME) || end.isAfter(CLOSING_TIME)) {
```

Now, if the university changes its opening hours, we change **one line**.
The same is true for error messages: write them once (as constants or in one exception class),
exactly as SPEC.md says. Do not copy the same text into many places.

### 4.5 Do not repeat yourself (DRY)

If you copy the same 5 lines into 3 places, a bug must be fixed in 3 places.
Move them into **one** private method or into a shared class (for example, all time parsing
lives in `TimeRules`). **Before you write a helper, search the project — maybe it already exists.**

---

## 5. Java-specific rules

| Rule | Do | Don't |
|---|---|---|
| Use `final` for values that do not change | `private final RoomRepository roomRepository;` | non-final injected fields |
| Constructor injection only | constructor with parameters | `@Autowired` on fields |
| Use records for DTOs | `public record RoomResponse(Long id, String name, int capacity) {}` | DTO classes with setters |
| Return copies, not internal lists | `return List.copyOf(bookings);` | `return bookings;` (caller can change it) |
| `Optional` only as a return type | `Optional<Room> findByName(String name)` | `Optional` fields or parameters |
| Compare strings with `equals` | `name.equals(other)` | `name == other` |
| Use `java.time` | `LocalDate`, `LocalTime` | `Date`, `Calendar` |
| Use `var` only when the type is obvious | `var rooms = new ArrayList<Room>();` | `var x = service.process();` |
| Use the logger | `log.info("Room {} created", id);` | `System.out.println(...)` |
| Throw specific exceptions | `throw new RoomNotFoundException(id);` | `throw new RuntimeException("error");` |
| Never swallow errors | handle it, or let it go up | `catch (Exception e) { }` |

### 5.1 Respect the layers (from SPEC.md)

```
Controller  →  Service  →  Repository  →  Database
```

- **Controller:** thin. Request DTO → call the service → return a response DTO. No business rules, no queries.
- **Service:** **all** business rules and validation. Throws exceptions from `exception/`.
- **Repository:** queries only. No business rules.
- A layer only talks to the layer **directly below it**. A controller never calls a repository.

### 5.2 Input validation and safety

- Validate all user input at the boundary (service), with clear messages from SPEC.md.
- Never build SQL with `+` string concatenation. Use Spring Data methods or `@Query` with parameters.
- **No secrets in the code.** Passwords and database URLs come from environment variables / `.env`
  (which is **not** committed). Use `.env.example` to show which variables are needed.
- Error responses must not show stack traces or database details to the user.

---

## 6. Comments and Javadoc

Good code explains **what** it does by itself. Comments explain **why**.

| ❌ Bad comments | ✅ Good comments |
|---|---|
| say the obvious: `i++; // add 1 to i` | explain a reason: `// University rule: rooms close at 22:00` |
| old code left in comments (Git keeps history) | a warning: `// Do not change the order: overlap check needs sorted list` |
| an excuse for bad code | `// TODO(Huseyn): remove after Neon migration` — always with a name |
| comments that are no longer true | Javadoc for public service methods |

**Javadoc** is the Java way to document classes and methods. It starts with `/**`.
Tags: `@param`, `@return`, `@throws`. IntelliJ shows it when you hover over the method.
Write Javadoc for **public methods in services** and for anything that is not obvious.

```java
/**
 * Creates a booking for a room on a given date and time.
 *
 * <p>Two bookings in the same room may not overlap. A booking that ends at 10:00
 * and another that starts at 10:00 do <b>not</b> overlap.
 *
 * @param request the room id, date, start time and end time (as strings, see SPEC.md)
 * @return the saved booking
 * @throws InvalidInputException    if the date or time is missing or has a wrong format
 * @throws RoomNotFoundException    if no room has this id
 * @throws BookingConflictException if the room is already booked at this time
 */
public Booking createBooking(CreateBookingRequest request) {
    ...
}
```

---

## 7. Tests

- Every service method has unit tests. Test the **normal case** and the **edge cases**:
  empty list, `0`, `null`, a time exactly at the border (10:00–10:00), a room that does not exist.
- Test names describe the behaviour: `createBooking_whenRoomIsAlreadyBooked_throwsConflict()`.
- Use the **Arrange – Act – Assert** structure:

```java
@Test
void findFreeRooms_whenOneRoomIsBooked_returnsOnlyTheOtherRoom() {
    // Arrange
    Room roomA = saveRoom("A-101", 30);
    Room roomB = saveRoom("B-202", 20);
    saveBooking(roomA, "2026-12-01", "10:00", "12:00");

    // Act
    List<Room> freeRooms = roomService.findFreeRooms("2026-12-01", "10:30", "11:00", 0);

    // Assert
    assertEquals(List.of(roomB), freeRooms);
}
```

- Run `./mvnw test` before every push. **All tests must pass.**

---

## 8. Code review

Every change goes into `main` through a **Pull Request**, and **one other team member reviews it**.
A bug found in review is much cheaper than a bug found by a user, and both people learn.

### 8.1 Review checklist (author checks first, then reviewer)

| Question | Check |
|---|---|
| **Correct** | Does it do what SPEC.md says? Edge cases: empty list, `0`, `null`, border times? |
| **Readable** | Good names? Methods under 20 lines? Nesting max 3 levels? |
| **Simple** | Is there a simpler way? Duplicated code? Magic numbers? |
| **Layers** | Business rules only in the service? Controller thin? |
| **Safe** | Input validated? Errors handled (nothing swallowed)? No secrets, no `.env` committed? |
| **Tested** | New tests added? `./mvnw test` passes? |
| **Standard** | Formatted with Ctrl+Alt+L? No `System.out.println`, no unused imports, no commented-out code? |

### 8.2 How to give and receive feedback

- Talk about the **code**, not the person: "this method is long", not "you write long methods".
- **Ask questions:** "What happens when the list is empty?"
- Give a **reason** and, if you can, a **suggestion**.
- Say what is **good**, too.
- Do not argue about style that the IDE formatter can fix.
- Keep Pull Requests **small**. Nobody can review 2 000 lines well.
- As the author: do not take it personally, answer every comment, and say thank you.

> ✅ **Good comment:** "Line 42: if `rooms` is empty, `get(0)` throws an exception.
> Can we return `Optional.empty()` here, or check `rooms.isEmpty()` first?"
>
> ⚠️ **Bad comment:** "This is wrong. Rewrite it."
