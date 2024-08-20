// File managed by WebFX (DO NOT EDIT MANUALLY)

module AstroRunner.application {

    // Direct dependencies modules
    requires javafx.graphics;
    requires javafx.media;
    requires webfx.extras.canvas.pane;
    requires webfx.platform.resource;
    requires webfx.platform.scheduler;

    // Exported packages
    exports com.orangomango.astrorunner;

    // Resources packages
    opens audio;
    opens files;
    opens images;

    // Provided services
    provides javafx.application.Application with com.orangomango.astrorunner.MainApplication;

}