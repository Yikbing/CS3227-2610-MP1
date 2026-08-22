# Study Tracker Developer Guide

## Introduction

Study Tracker is a JavaFX desktop application for recording completed study sessions and reviewing how study time is distributed across modules. This guide explains the design and implementation decisions that a developer needs to understand when maintaining or extending the application.

## Getting started

### Prerequisites

- JDK 25
- An IDE with Gradle support, such as IntelliJ IDEA, or a terminal

The repository includes the Gradle Wrapper, so a separate Gradle installation is not required. Ensure that both the project SDK and the Gradle JVM use JDK 25.

### Running the application

From the repository root, run:

```shell
# Windows
.\gradlew.bat run

# macOS or Linux
./gradlew run
```

Alternatively, import the project as a Gradle project in an IDE and run `studytracker.Launcher`.

### Running the tests

Run all automated tests with:

```shell
# Windows
.\gradlew.bat test

# macOS or Linux
./gradlew test
```

A successful test run produces the HTML report at `build/reports/tests/test/index.html`.

## Design

### Overall architecture

Study Tracker uses a small layered design. A typical request moves through the application as follows:

```text
JavaFX UI -> StudyTracker -> Parser -> Command -> Model
                         \-> Storage (after a successful change)
```

`StudyTracker` is the application coordinator. It owns the parser, storage component, and in-memory session list. This keeps persistence policy out of individual commands and prevents the UI from manipulating model data directly.

### Component responsibilities

| Component | Responsibility |
| --- | --- |
| `Main` and `MainWindow` | Start JavaFX, collect command text, display feedback or errors, and close the application when requested. |
| `StudyTracker` | Load initial data and coordinate parsing, command execution, and conditional saving. |
| `Parser`, `ArgumentMap`, and `ParserUtil` | Recognise the command word, extract prefixed arguments, convert values, and reject invalid command syntax. |
| `Command` implementations | Represent and execute one user operation against `SessionList`. |
| `StudySession`, `SessionList`, `EditDescriptor`, and `StudyStatistics` | Represent session data, enforce domain rules, and provide collection operations and derived statistics. |
| `Storage` and `SessionFileCodec` | Access the local data file and convert sessions to and from the persistent format. |

The UI catches `StudyTrackerException` and displays its message to the user. Expected input, model, and storage errors therefore do not expose stack traces through the interface.

### Main data model

#### `StudySession`

`StudySession` is an immutable record of one completed period of study. It contains a module, topic, duration in minutes, date, and optional notes. Its constructor enforces the following invariants:

- module and topic must not be blank;
- duration must be a positive whole number;
- date must not be `null`;
- `null` notes are normalised to an empty string.

Using an immutable record means a valid session cannot later be placed into an invalid intermediate state through setters. It also provides value-based equality, which is useful when comparing saved and loaded sessions in tests.

#### `SessionList`

`SessionList` owns the mutable collection and exposes the application's domain operations. Commands use this class instead of accessing its internal `ArrayList` directly.

Important behavior includes:

- displayed session numbers are one-based and are converted to zero-based list indices internally;
- `find` performs a case-insensitive substring search across module, topic, and notes;
- `filter` can combine a case-insensitive module match with an inclusive date range;
- filtered results are ordered from newest to oldest;
- statistics contain the session count, total duration, and duration grouped by module;
- `asList()` returns an unmodifiable copy, preventing callers from changing the internal list directly.

#### `EditDescriptor`

`EditDescriptor` stores only the fields supplied in an `edit` command. An absent field preserves its existing value. Applying the descriptor constructs a new `StudySession`, so all normal session validation occurs before the original entry is replaced.

An omitted `n/` prefix preserves the existing notes. Supplying `n/` with an empty value clears them.

#### `StudyStatistics`

`StudyStatistics` is an immutable summary derived from the current sessions. It is recalculated when the `stats` command is executed rather than stored separately, so persistent statistics cannot become inconsistent with the underlying session records.

## Implementation

### Command processing flow

For a command such as:

```text
add m/CS3227 t/Command parsing d/90 on/2026-08-17 n/Parser tests
```

the application performs these steps:

1. `MainWindow` passes the raw text to `StudyTracker.execute`.
2. `Parser` separates the command word from its arguments and selects the corresponding parsing method.
3. `ArgumentMap` extracts named values such as `m/`, `t/`, and `d/`. It rejects prefixes that are unsupported by that command and prefixes supplied more than once.
4. `ParserUtil` converts the duration and date, while `StudySession` enforces the domain invariants.
5. `Parser` returns an `AddCommand` containing the validated session.
6. The command adds the session to `SessionList` and returns a changed `CommandResult`.
7. Because `dataChanged()` is `true`, `StudyTracker` saves the updated session list.
8. The result feedback returns to `MainWindow` for display.

Read-only commands follow the same general path but return `CommandResult.readOnly(...)`. They do not trigger a file write. `CommandResult` also carries the exit flag used by `MainWindow` to close the application after an `exit` command.

### Editing a study session

The parser reads the one-based session number separately from the prefixed fields. At least one field must be supplied. It then creates an `EditDescriptor` containing an `Optional` value for each field that should change.

During execution, `SessionList` validates the index, applies the descriptor to the original session, and replaces that entry with the newly constructed session. Because construction occurs before replacement, failed validation does not leave a partially edited session in the list.

### Searching, filtering, and statistics

These operations are implemented in `SessionList` and do not mutate stored sessions:

- `find` normalises the keyword and searchable text to lower case before checking for substrings;
- `filter` applies only the criteria supplied by the user, treats both date boundaries as inclusive, and sorts matching sessions by descending date;
- `calculateStatistics` sums all durations and merges minutes belonging to modules with case-insensitive names.

Keeping these operations in the model lets commands remain small: each command requests an operation and formats its result for the user.

### Data loading and persistence

The default data file is `data/sessions.txt`. If it does not exist, the application starts with an empty session list. If loading fails because the file cannot be read or contains invalid data, `StudyTracker` opens an empty list and places the error explanation in its startup message.

Each nonblank line in the file represents one session with the following tab-separated fields:

```text
version  module  topic  durationMinutes  date  notes
```

The current format version is `1`. Text fields are Base64-encoded UTF-8, which allows tabs, delimiter-like text, and Unicode to round-trip safely, although it makes the file less convenient for manual editing. Dates use the ISO `YYYY-MM-DD` representation.

When saving, `Storage` creates the parent directory if necessary and writes all sessions to a temporary sibling file. It first attempts to replace the data file using an atomic move and falls back to a normal replacement if that move fails. This reduces the chance of leaving a partially written data file.

### Error handling

The application uses a small exception hierarchy for expected failures:

- `ParseException` reports invalid command syntax or values;
- `StudyTrackerException` reports application-level problems, including invalid session numbers;
- `StorageException` reports failures when decoding, reading, or writing saved data.

`MainWindow` catches these exceptions and displays a friendly error. A parsing or model error occurs before saving. For a mutating command, however, the in-memory model is changed before `Storage.save` runs. If saving fails, the error is reported but the in-memory change is not rolled back. Transactional rollback would provide stronger consistency but would add complexity that is not necessary for the current project scope.

## Design considerations

### Prefixed arguments

**Current choice:** Commands use named prefixes, for example `m/CS3227` and `d/90`. This supports multi-word values, optional fields, and flexible ordering of prefixed arguments.

**Alternative:** Positional arguments would require less extraction logic, but optional and multi-word fields would need stricter ordering or quoting rules. Named prefixes are clearer for the current command set.

### Immutable study sessions

**Current choice:** Editing creates a replacement immutable `StudySession`. Validation happens during construction, and there are no setters that can independently violate session invariants.

**Alternative:** A mutable class with setters could make individual assignments simpler, but an edit involving several fields could be only partially applied if later validation failed. Immutability is sufficiently simple and safer for this small model.

### Versioned line-based storage

**Current choice:** Sessions use a versioned line format with Base64-encoded text. It requires no additional serialization library, safely preserves arbitrary text, and provides a version marker for recognising incompatible formats.

**Alternatives:** JSON would be easier for humans to inspect and structurally extend, but would normally require a dependency or additional parsing code. Java object serialization would reduce explicit mapping code but tightly couple stored data to Java class definitions. The controlled text format is adequate for the application's current data size and structure.

### Automatic persistence after changes

**Current choice:** A command reports whether it changed data through `CommandResult`. `StudyTracker` saves immediately after successful changes and skips saving after read-only commands. Centralising this decision avoids duplicating storage calls in every mutating command and reduces the amount of work lost after an unexpected shutdown.

**Alternative:** Saving only on exit would perform fewer writes, but all changes from the current run could be lost if the application terminated unexpectedly. The current approach is more appropriate for a personal tracker with a small data file.

## Testing

### Test organisation

The JUnit 5 tests under `src/test/java/studytracker` are grouped by the responsibility they exercise:

```text
src/test/java/studytracker/
|-- StudyTrackerTest.java
|-- command/
|   `-- CommandTest.java
|-- model/
|   |-- EditDescriptorTest.java
|   |-- SessionListTest.java
|   |-- StudySessionTest.java
|   `-- StudyStatisticsTest.java
|-- parser/
|   `-- ParserTest.java
`-- storage/
    |-- SessionFileCodecTest.java
    `-- StorageTest.java
```

Storage and coordinator integration tests use JUnit's `@TempDir`, so they operate on isolated temporary files rather than the user's real `data/sessions.txt`.

### Existing test coverage

The current automated tests cover:

- model validation, normalisation, formatting, immutable views, edits, index boundaries, searching, filtering, and statistics;
- successful, invalid, and boundary inputs for all nine command formats;
- execution, feedback, model effects, and `CommandResult` flags for every command;
- storage encoding, decoding, round trips, replacement, malformed records, and stable read/write failures; and
- coordinator startup, load recovery, conditional saving, command failures, save failures, and restart persistence.

The suite is comprehensive against the documented non-UI behavior, but it is not intended to claim exhaustive testing of every possible input, operating system, or filesystem condition.

### Testing strategy and exclusions

The suite primarily uses black-box tests derived from documented behavior. Implementation-informed branch and boundary analysis supplements those tests where it exposes meaningful error paths. Tests interact through public class methods rather than calling private helpers directly.

Equivalent inputs are grouped using parameterised tests and equivalence partitioning. Unit tests verify individual responsibilities, while command and `StudyTracker` integration tests verify component boundaries without repeating every lower-level assertion.

The JavaFX interface is verified using the [manual JavaFX test script](ManualUiTestScript.md). It covers application startup, Enter-key and button submission, displayed feedback, invalid-input recovery, persistence, malformed-data recovery, resizing, styling, and exit behavior. Automated JavaFX tests are intentionally excluded to avoid adding platform-sensitive UI-testing infrastructure to this introductory project.

The fallback from an atomic file move to a normal replacement is also intentionally excluded from automated testing because reliably forcing that filesystem condition would be operating-system dependent or require production-code changes solely for failure injection.
