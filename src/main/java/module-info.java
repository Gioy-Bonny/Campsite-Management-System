module campsite.management {
    // JavaFX controls (also pulls in javafx.graphics and javafx.base)
    requires javafx.controls;

    // JavaFX needs to launch your Application class
    exports campsite;
    exports campsite.ui;

    // The other layers, so tests and other code can see them
    exports campsite.structures;
    exports campsite.model;
    exports campsite.service;

    // Only needed if you use TableView with PropertyValueFactory later:
    // JavaFX reads your model getters by reflection
    opens campsite.model to javafx.base;
}