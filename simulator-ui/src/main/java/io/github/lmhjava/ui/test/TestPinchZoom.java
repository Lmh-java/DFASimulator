package io.github.lmhjava.ui.test;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class TestPinchZoom extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(new Button("Button 1"));
        hBox.getChildren().add(new Button("Button 2"));

        hBox.setOnZoom(e -> {
            System.out.println("zoom code engaged");
            hBox.setScaleX(hBox.getScaleX() * e.getZoomFactor());
            hBox.setScaleY(hBox.getScaleY() * e.getZoomFactor());
        });

        Scene mySceneGraph = new Scene(hBox, 800, 600);
        primaryStage.setScene(mySceneGraph);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
