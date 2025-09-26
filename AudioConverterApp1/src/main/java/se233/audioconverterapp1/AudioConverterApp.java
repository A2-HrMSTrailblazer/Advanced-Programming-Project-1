package se233.audioconverterapp1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AudioConverterApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(AudioConverterApp.class.getResource("audioconverter.fxml"));
        stage.setTitle("Audio Converter App");
        stage.setScene(new Scene(loader.load(), 800, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
