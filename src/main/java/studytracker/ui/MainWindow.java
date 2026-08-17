package studytracker.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import studytracker.StudyTracker;
import studytracker.command.CommandResult;
import studytracker.exception.StudyTrackerException;

/** Controls the main command-oriented desktop window. */
public class MainWindow {
    @FXML
    private TextArea outputArea;
    @FXML
    private TextField commandInput;
    @FXML
    private Button executeButton;

    private StudyTracker studyTracker;

    public void setStudyTracker(StudyTracker studyTracker) {
        this.studyTracker = studyTracker;
        outputArea.setText(studyTracker.getStartupMessage());
        commandInput.requestFocus();
    }

    /** Processes the current input and displays either its result or a friendly error. */
    @FXML
    private void handleCommand() {
        String input = commandInput.getText();
        if (studyTracker == null) {
            return;
        }
        append("\n\n> " + input);
        try {
            CommandResult result = studyTracker.execute(input);
            append("\n" + result.feedback());
            commandInput.clear();
            if (result.exit()) {
                commandInput.setDisable(true);
                executeButton.setDisable(true);
                Platform.exit();
            }
        } catch (StudyTrackerException exception) {
            append("\nError: " + exception.getMessage());
        }
    }

    private void append(String text) {
        outputArea.appendText(text);
        outputArea.positionCaret(outputArea.getLength());
    }
}
