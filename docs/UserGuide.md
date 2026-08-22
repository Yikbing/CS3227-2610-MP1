# Study Tracker User Guide

## Introduction

Study Tracker is a JavaFX desktop application for recording completed study sessions. It lets you maintain a study history, search and filter past sessions, and review the amount of time spent on each module.

You interact with Study Tracker by entering commands into the text field at the bottom of the application window.

## Quick start

### Prerequisites

Before running Study Tracker, ensure that you have:

- JDK 25 installed;
- an IDE with Gradle support, such as IntelliJ IDEA, or a terminal; and
- the Study Tracker project files.

The project includes the Gradle Wrapper, so a separate Gradle installation is not required. If you use an IDE, ensure that both the project SDK and the Gradle JVM are set to JDK 25.

### Running Study Tracker

From the project root, run the command for your operating system:

```shell
# Windows
.\gradlew.bat run

# macOS or Linux
./gradlew run
```

Alternatively, import the project as a Gradle project in your IDE and run the `studytracker.Launcher` class.

When Study Tracker starts, it displays a welcome message. Enter a command in the text field and either press Enter or select the **Execute** button. The command and its result will appear in the output area.

## Command conventions

The command examples in this guide use the following conventions:

- Words in uppercase, such as `MODULE`, represent values that you must supply.
- Items in square brackets, such as `[n/NOTES]`, are optional. Do not type the square brackets.
- Command words such as `add` and `LIST` are case-insensitive.
- Prefixes such as `m/`, `t/`, and `on/` are case-sensitive and must be lowercase.
- Dates must use the `YYYY-MM-DD` format and must be valid calendar dates.
- Durations and session indexes must be positive whole numbers.
- Values may contain spaces; quotation marks are not required.
- Prefixed arguments may be supplied in any order.
- A prefix cannot be repeated in the same command.
- Extra text and prefixes that are not supported by a command are rejected.
- Session numbering starts at `1`.

> **Note:** The numbers displayed by `list` identify sessions for the `edit` and `delete` commands. Search and filter results are numbered independently, so run `list` before editing or deleting a session found through `find` or `filter`.

## Features

### Viewing help: `help`

Displays a summary of all supported command formats.

Format:

```text
help
```

Example:

```text
help
```

The command does not accept additional arguments.

### Recording a study session: `add`

Records one completed study session.

Format:

```text
add m/MODULE t/TOPIC d/MINUTES on/YYYY-MM-DD [n/NOTES]
```

Parameters:

| Parameter | Required | Description |
| --- | --- | --- |
| `m/MODULE` | Yes | Module studied. It must not be blank. |
| `t/TOPIC` | Yes | Topic studied. It must not be blank. |
| `d/MINUTES` | Yes | Duration as a positive whole number of minutes. |
| `on/YYYY-MM-DD` | Yes | Date on which the study session was completed. |
| `n/NOTES` | No | Additional notes about the session. |

Examples:

```text
add m/CS3227 t/Command parsing d/90 on/2026-08-17 n/Parser tests
add m/CS2100 t/Assembly language d/60 on/2026-08-18
```

After a successful command, Study Tracker displays the recorded session and saves it automatically.

### Listing all study sessions: `list`

Displays every recorded session in the order in which it was added.

Format:

```text
list
```

Example:

```text
list
```

Each session is assigned a number. Use the number shown here when editing or deleting that session. If no sessions have been recorded, Study Tracker displays `No matching study sessions.`

The command does not accept additional arguments.

### Editing a study session: `edit`

Changes one or more fields of an existing session.

Format:

```text
edit INDEX [m/MODULE] [t/TOPIC] [d/MINUTES] [on/YYYY-MM-DD] [n/NOTES]
```

At least one field must be supplied. Fields that are not supplied retain their existing values.

Examples:

```text
edit 1 d/120
edit 2 t/Virtual memory on/2026-08-19 n/Reviewed lecture notes
```

To remove the notes from a session, supply an empty notes value:

```text
edit 1 n/
```

The index must refer to a session in the full list produced by `list`. After a successful edit, Study Tracker displays the updated session and saves the change automatically.

### Deleting a study session: `delete`

Permanently removes an existing session.

Format:

```text
delete INDEX
```

Example:

```text
delete 2
```

The index must refer to a session in the full list produced by `list`. After a successful deletion, Study Tracker displays the deleted session and saves the change automatically.

### Finding study sessions: `find`

Finds sessions containing a keyword in their module, topic, or notes.

Format:

```text
find KEYWORD
```

Example:

```text
find parsing
```

Matching is case-insensitive and checks whether the keyword occurs anywhere within a module, topic, or note. For example, `find parse` can match a topic containing `Parser`.

The keyword must not be blank. Results retain the order in which the matching sessions were added. Running `find` does not alter saved data.

### Filtering study sessions: `filter`

Filters sessions by module, date, or a combination of those criteria.

Format:

```text
filter [m/MODULE] [from/YYYY-MM-DD] [to/YYYY-MM-DD]
```

At least one filter must be supplied.

Examples:

```text
filter m/CS3227
filter from/2026-08-01 to/2026-08-31
filter m/CS3227 from/2026-08-01 to/2026-08-31
```

The module name must match the complete stored module name, although capitalisation is ignored. For example, `m/cs3227` matches `CS3227`, but `m/CS32` does not.

The `from/` and `to/` boundaries are inclusive. If both are supplied, the `from/` date cannot be later than the `to/` date. Results are displayed from newest to oldest. Running `filter` does not alter saved data.

### Viewing study statistics: `stats`

Displays:

- the total number of recorded sessions;
- the total study time; and
- the study time accumulated for each module.

Format:

```text
stats
```

Example:

```text
stats
```

Module names are grouped without regard to capitalisation. For example, sessions recorded under `CS3227` and `cs3227` contribute to the same module total. Durations are displayed in hours and minutes where appropriate.

If no sessions have been recorded, Study Tracker displays `No study sessions have been recorded yet.` The command does not accept additional arguments and does not alter saved data.

### Exiting Study Tracker: `exit`

Closes Study Tracker.

Format:

```text
exit
```

Example:

```text
exit
```

The command does not accept additional arguments. Study Tracker saves each successful addition, edit, or deletion immediately, so those changes are already stored before you exit.

## Data storage

Study Tracker stores sessions locally in `data/sessions.txt`, relative to the directory from which the application is run. The `data` directory and file are created automatically when the first change is saved.

If the file does not exist, Study Tracker starts with an empty session list. If the file cannot be read or contains invalid data, Study Tracker opens an empty session list and displays an explanation at startup.

The storage file uses an application-specific encoded format and is not intended for manual editing. Editing it directly may prevent the saved sessions from loading.

## Feature walkthrough

The following walkthrough demonstrates the main workflow and every supported command. For predictable results, begin with an empty session list. If you already have saved data that you want to keep, make a copy of `data/sessions.txt` before performing the walkthrough.

1. Start Study Tracker and enter `help`.
   - The application should display all nine supported command formats.
2. Enter:

   ```text
   add m/CS3227 t/Command parsing d/90 on/2026-08-17 n/Parser tests
   ```

   - The application should confirm that the CS3227 session was recorded.
3. Enter:

   ```text
   add m/CS2100 t/Assembly language d/60 on/2026-08-18
   ```

   - The application should confirm that the CS2100 session was recorded.
4. Enter `list`.
   - Two sessions should appear in insertion order. CS3227 should be session `1`, and CS2100 should be session `2`.
5. Enter `find parsing`.
   - Only the CS3227 session should appear.
6. Enter `filter m/CS3227`.
   - Only the CS3227 session should appear.
7. Enter `filter from/2026-08-18`.
   - Only the CS2100 session should appear because the boundary is inclusive.
8. Enter `stats`.
   - The summary should show two sessions, a total of `2 h 30 min`, `1 h` for CS2100, and `1 h 30 min` for CS3227.
9. Enter `edit 1 d/120`.
   - The CS3227 session should now have a duration of `2 h`; its other fields should be unchanged.
10. Enter `delete 2`.
    - The CS2100 session should be removed.
11. Enter `list`.
    - Only the edited CS3227 session should remain.
12. Enter `exit`, then start Study Tracker again and enter `list`.
    - The CS3227 session should still be present, confirming that saved data was loaded.

The walkthrough changes your real saved data. You may delete the remaining test session through the application when finished.

## Common input errors

Study Tracker displays an error message instead of executing an invalid command. Common causes include:

| Problem | Invalid example | Correction |
| --- | --- | --- |
| Unknown command | `show` | Use `list` or enter `help` to see available commands. |
| Missing required field | `add m/CS3227 d/60 on/2026-08-17` | Supply the missing topic using `t/TOPIC`. |
| Invalid date format | `on/17-08-2026` | Use `on/2026-08-17`. |
| Invalid calendar date | `on/2026-02-30` | Supply a date that exists. |
| Non-positive duration | `d/0` | Supply a positive whole number, such as `d/30`. |
| Invalid session index | `delete 0` | Run `list` and use one of its displayed indexes. |
| Edit without changes | `edit 1` | Supply at least one field to change. |
| Filter without criteria | `filter` | Supply `m/`, `from/`, or `to/`. |
| Repeated prefix | `filter m/CS3227 m/CS2100` | Supply each prefix at most once. |
| Unsupported prefix | `filter t/Parsing` | Use only `m/`, `from/`, and `to/` with `filter`. |

## Troubleshooting

### The application does not start

Verify that JDK 25 is installed and that the `java` toolchain used by Gradle is JDK 25. In an IDE, check both the project SDK and Gradle JVM settings.

### Saved sessions are not appearing

Run Study Tracker from the same project directory each time. The data path is relative to the application's working directory, so running it from a different directory may cause it to use a different `data/sessions.txt` file.

Also check the startup message. If Study Tracker could not read the saved data, it will explain that it opened an empty session list.

### A session cannot be edited or deleted

Run `list` and use the index displayed in the complete study history. Do not rely on the numbering shown by `find` or `filter`, because those commands number only their result sets.

## Command summary

| Command | Format |
| --- | --- |
| Help | `help` |
| Add | `add m/MODULE t/TOPIC d/MINUTES on/YYYY-MM-DD [n/NOTES]` |
| List | `list` |
| Edit | `edit INDEX [m/MODULE] [t/TOPIC] [d/MINUTES] [on/YYYY-MM-DD] [n/NOTES]` |
| Delete | `delete INDEX` |
| Find | `find KEYWORD` |
| Filter | `filter [m/MODULE] [from/YYYY-MM-DD] [to/YYYY-MM-DD]` |
| Statistics | `stats` |
| Exit | `exit` |
