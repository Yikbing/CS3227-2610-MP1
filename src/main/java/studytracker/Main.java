package studytracker;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import studytracker.ui.MainWindow;

/** Starts the Study Tracker JavaFX application. */
public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
        Scene scene = new Scene(loader.load(), 760, 560);
        scene.getStylesheets().add(Main.class.getResource("/css/application.css").toExternalForm());

        MainWindow controller = loader.getController();
        controller.setStudyTracker(StudyTracker.createDefault());

        stage.setTitle("Study Tracker");
        stage.setMinWidth(620);
        stage.setMinHeight(440);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
