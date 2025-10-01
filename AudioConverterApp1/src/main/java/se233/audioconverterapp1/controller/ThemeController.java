package se233.audioconverterapp1.controller;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;

public class ThemeController {
    private final StackPane dropContainer;
    private final CheckBox darkModeToggle;

    public ThemeController(StackPane dropContainer, CheckBox darkModeToggle) {
        this.dropContainer = dropContainer;
        this.darkModeToggle = darkModeToggle;
    }

    public void setup() {
        darkModeToggle.setOnAction(_ -> toggleDarkMode(darkModeToggle.isSelected()));
    }

    private void toggleDarkMode(boolean enable) {
        var scene = dropContainer.getScene();
        if (scene == null) return;

        scene.getStylesheets().clear();
        if (enable)
            scene.getStylesheets().add(getClass().getResource("/se233/audioconverterapp1/dark-theme.css").toExternalForm());
        else
            scene.getStylesheets().add(getClass().getResource("/se233/audioconverterapp1/ui.css").toExternalForm());
    }
}
