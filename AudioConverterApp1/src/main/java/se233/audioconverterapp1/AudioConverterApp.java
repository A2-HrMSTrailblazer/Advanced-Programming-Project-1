package se233.audioconverterapp1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AudioConverterApp extends Application {
    @Override
    public void start(@SuppressWarnings("exports") Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(AudioConverterApp.class.getResource("audioconverter.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 600);
        scene.getStylesheets().add(AudioConverterApp.class.getResource("ui.css").toExternalForm());
        stage.setTitle("Audio Converter App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
