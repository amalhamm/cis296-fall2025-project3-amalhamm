module com.example.javafxhelloworld {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens com.amal.project3 to javafx.fxml;
    exports com.amal.project3;
}