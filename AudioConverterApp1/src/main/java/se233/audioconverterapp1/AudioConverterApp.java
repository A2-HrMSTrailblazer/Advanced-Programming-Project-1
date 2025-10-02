package se233.audioconverterapp1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se233.audioconverterapp1.controller.AudioConverterController;
import se233.audioconverterapp1.view.ThemeController;

public class AudioConverterApp extends Application {
    @Override
    public void start(@SuppressWarnings("exports") Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(AudioConverterApp.class.getResource("audioconverter.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1200, 600);
        scene.getStylesheets().add(AudioConverterApp.class.getResource("ui.css").toExternalForm());
        stage.setTitle("Audio Converter App");
        stage.setScene(scene);
        stage.show();

        AudioConverterController controller = loader.getController();
        ThemeController themeController = new ThemeController(scene);
        controller.setThemeController(themeController);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
