# Manual JavaFX Test Script

Use this script after the automated Java 25 test suite passes. Run it from a disposable copy or working
directory because it deliberately changes `data/sessions.txt`. Back up any real study data first.

For every case, record **Actual result**, **Pass/Fail**, and any **Observation or defect severity**.

## Setup

1. Confirm Java 25 is active with `java -version`.
2. Back up and remove the test working directory's `data/sessions.txt`, if it exists.
3. Start the application with `./gradlew run` or `.\gradlew.bat run`.

## UI-01: Application startup and layout

1. Observe the window without entering a command.
2. Resize it down and then enlarge it.

Expected:

- The title is `Study Tracker`.
- The heading, subtitle, output area, command field, and Execute button are visible and styled.
- The welcome message refers to `help`.
- The command field receives focus and shows its prompt.
- The window does not shrink below its minimum size, and output remains readable when enlarged.

## UI-02: Help using the Enter key

1. Enter `help` and press Enter.

Expected:

- The entered command and help feedback are appended to the output area.
- All nine commands appear: add, list, edit, delete, find, filter, stats, help, and exit.
- The input field is cleared after success and regains focus for the next command.

## UI-03: Add using the Execute button

1. Enter `add m/CS3227 t/Command parsing d/90 on/2026-08-17 n/Parser tests`.
2. Select **Execute**.
3. Add `add m/CS2100 t/Assembly d/60 on/2026-08-18`.

Expected:

- Each addition displays confirmation containing the correct module, topic, duration, date, and notes.
- The first duration is displayed as `1 h 30 min`; the second as `1 h`.
- The input field clears after each successful command.

## UI-04: List and numbering

1. Enter `list`.

Expected:

- Two sessions appear in insertion order.
- CS3227 is numbered `1`; CS2100 is numbered `2`.

## UI-05: Find, filter, and statistics

1. Enter `find PARSING`.
2. Enter `filter m/cs3227`.
3. Enter `filter from/2026-08-18`.
4. Enter `filter m/CS3227 from/2026-08-01 to/2026-08-31`.
5. Enter `stats`.

Expected:

- Find returns only CS3227 despite keyword capitalisation.
- Module filtering is case-insensitive and returns only the complete module match.
- The lower date boundary is inclusive and returns CS2100.
- The combined filter returns only CS3227.
- Statistics report two sessions, `2 h 30 min` total, `1 h` for CS2100, and `1 h 30 min` for CS3227.

## UI-06: Edit and clear notes

1. Enter `edit 1 d/120`.
2. Enter `list` and verify only the duration changed.
3. Enter `edit 1 n/`.
4. Enter `list` and verify the notes are empty while all other fields remain unchanged.

Expected:

- Each edit displays updated-session feedback.
- Unspecified fields remain unchanged.
- `n/` removes the notes.

## UI-07: Delete

1. Enter `delete 2`.
2. Enter `list`.

Expected:

- The deletion feedback identifies CS2100.
- Only the edited CS3227 session remains.
- It is numbered `1` in the full list.

## UI-08: Invalid-input recovery

For each input below, enter it and observe the result:

1. An empty command.
2. `show`.
3. `add m/CS3227 t/Testing on/2026-08-17`.
4. `add m/CS3227 t/Testing d/30 on/2026-02-30`.
5. `delete 0`.
6. `delete 99`.

Expected:

- A friendly `Error:` message explains each problem.
- The application remains responsive and does not crash.
- Invalid input remains in the command field so it can be corrected.
- Entering `list` afterward confirms that invalid operations did not change data.

## UI-09: Persistence after restart

1. Close the window normally.
2. Start the application again from the same working directory.
3. Enter `list`.

Expected:

- The edited CS3227 session is loaded with its 120-minute duration and empty notes.
- The deleted CS2100 session does not reappear.

## UI-10: Malformed-data startup recovery

1. Close the application.
2. Back up the current `data/sessions.txt` used by this script.
3. Replace its contents with `not-a-valid-record`.
4. Start the application and enter `list`.

Expected:

- Startup explains that saved data could not be loaded and that an empty list was opened.
- `list` reports no matching sessions.
- The application remains usable.

Restore the script's valid data file afterward if it is needed for the final exit test.

## UI-11: Exit

1. Start the application if necessary.
2. Enter `exit`.

Expected:

- Exit is handled without an error or crash.
- The application window closes.

## Results summary

Record:

- Number of passed and failed cases.
- Any crash, data loss, or startup failure as **Critical**.
- Any blocked core command as **High**.
- Confusing behaviour requiring retries as **Medium**.
- Cosmetic or wording issues as **Low**.
