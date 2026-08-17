# Study Tracker Developer Guide

## Architecture

Study Tracker uses a small layered design:

1. `ui` collects commands and displays results.
2. `parser` turns command text into a `Command` object.
3. `command` coordinates one operation on the model.
4. `model` owns session data, validation, filtering, and statistics.
5. `storage` converts sessions to a versioned text format and accesses the local file.

`StudyTracker` is the application coordinator. After a command executes successfully, it saves only when `CommandResult.dataChanged()` is true. This keeps read-only commands from performing unnecessary writes.

## Model

`StudySession` is immutable. An edit creates a replacement session using `EditDescriptor`, which prevents partially applied updates if validation fails.

Durations are stored as positive whole minutes and dates use `LocalDate`. Notes are optional; module and topic are required.

## Storage format

Each nonblank line represents one session. The line begins with a format version and contains tab-separated fields. Text fields use Base64-encoded UTF-8, allowing notes and topics to contain delimiters and Unicode safely.

Saving first writes a temporary sibling file and then replaces the data file. An atomic move is attempted when supported by the filesystem.

## Building and testing

Prerequisites:

- JDK 25
- Gradle, or an IDE capable of importing the Gradle build

Commands:

```text
gradle test
gradle run
```

Tests are grouped by parser, model, and storage responsibility under `src/test/java`.

## Future extension

Planned study topics should be represented separately from completed `StudySession` records. They should not contribute to actual-time statistics. Introduce `PlannedTopic` and `PlannedTopicList` only when that feature is implemented rather than adding speculative inheritance now.
