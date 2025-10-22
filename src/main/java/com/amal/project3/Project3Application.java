//main JavaFX entry point (stage, first scene to choose nb of players...)
package com.amal.project3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.*;

public class Project3Application extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Project3Application.class.getResource("hello-view.fxml"));// open the first scene
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);// eight and width of the first scene
        stage.setTitle("Monopoly Game");
        stage.setScene(scene);  // setting the starter scene
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}