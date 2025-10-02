package se233.audioconverterapp1.view;

import javafx.scene.Scene;

public class ThemeController {
    private final Scene scene;
    
    public ThemeController(Scene scene) {
        this.scene = scene;
    }

    // Dark Mode Setup
    public void toggleDarkMode(boolean enable) {
        if (scene == null) return;

        scene.getStylesheets().clear();
        if (enable) scene.getStylesheets().add(getClass().getResource("/se233/audioconverterapp1/dark-theme.css").toExternalForm());
        else scene.getStylesheets().add(getClass().getResource("/se233/audioconverterapp1/ui.css").toExternalForm());
    }
}
