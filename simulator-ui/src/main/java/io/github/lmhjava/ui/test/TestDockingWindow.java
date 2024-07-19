package io.github.lmhjava.ui.test;

import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class TestDockingWindow extends Application {

    @Override
    public void start(Stage stage) {
        SplitPane sp = new SplitPane();
        ObservableList<Node> list = sp.getItems();
        DetachableTabPane detachableTabPane = new DetachableTabPane();
        list.add(detachableTabPane);
        detachableTabPane.addTab("test1", new Circle());
        detachableTabPane.addTab("test2", new Circle());
        Scene scene = new Scene(new StackPane(sp), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
