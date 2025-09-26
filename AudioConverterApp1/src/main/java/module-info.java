module se233.audioconverterapp1 {
    requires javafx.controls;
    requires javafx.fxml;

    // no explicit requires for ffmpeg; Maven/Java will treat it as automatic
    //requires slf4j.simple;  // logger backend
    requires org.slf4j;     // slf4j API

    opens se233.audioconverterapp1 to javafx.fxml;
    opens se233.audioconverterapp1.controller to javafx.fxml;
    exports se233.audioconverterapp1;
    exports se233.audioconverterapp1.controller;
}
