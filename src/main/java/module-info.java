module com.raycaster {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.raycaster to javafx.fxml;
    exports com.raycaster;
}
