package se233.audioconverterapp1.exception;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AppExceptionHandler {

    public static void handle(Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");

            String header = getHeader(e);
            alert.setHeaderText(header);
            alert.setContentText(e.getMessage() != null ? e.getMessage() : "An unknown error occurred.");

            // Optional: show details
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionText = sw.toString();
            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxHeight(200);

            alert.getDialogPane().setExpandableContent(textArea);
            alert.showAndWait();
        });
    }

    private static String getHeader(Exception e) {
        if (e instanceof DuplicateOutputException)
            return "Duplicate Output File Detected";
        if (e instanceof MissingFFmpegException)
            return "FFmpeg Configuration Missing";
        if (e instanceof InvalidAudioFormatException)
            return "Invalid Audio Format";
        if (e instanceof ConversionFailureException)
            return "Conversion Failed";
        return "Unexpected Error";
    }
}
