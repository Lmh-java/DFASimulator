package io.github.lmhjava.ui.test;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class TestPanningScrollPane extends Application {

    double scale;
    Pane contPane = new Pane();

    @Override
    public void start(Stage primaryStage) {
        BorderPane pane = new BorderPane();
        ScrollPane sp = new ScrollPane();
        sp.setContent(new Group(contPane));
        sp.setVvalue(0.5);
        sp.setHvalue(0.5);
        Rectangle rec = new Rectangle(2820, 1240,Color.RED);
        scale = 0.2;
        contPane.setScaleX(scale);
        contPane.setScaleY(scale);

        contPane.getChildren().add(rec);

        Button but1 = new Button("+");
        but1.setOnAction((ActionEvent event) -> {
            scale*=2;
            contPane.setScaleX(scale);
            contPane.setScaleY(scale);
        });
        Button but2 = new Button("-");
        but2.setOnAction((ActionEvent event) -> {
            scale/=2;
            contPane.setScaleX(scale);
            contPane.setScaleY(scale);
        });

        contPane.setOnZoom(e -> {
            contPane.setScaleX(contPane.getScaleX() * e.getZoomFactor());
            contPane.setScaleY(contPane.getScaleY() * e.getZoomFactor());
        });
        HBox buttons = new HBox(but1, but2);
        pane.setTop(buttons);
        pane.setCenter(sp);
        Scene scene = new Scene(pane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
