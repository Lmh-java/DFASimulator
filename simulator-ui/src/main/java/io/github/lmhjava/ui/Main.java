package io.github.lmhjava.ui;

import io.github.lmhjava.ui.controller.CanvasController;
import io.github.lmhjava.ui.model.CanvasModel;
import io.github.lmhjava.ui.util.ScreenUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.BasicConfigurator;

public class Main extends Application {

    private final static String APP_TITLE = "DFA Simulator";

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CanvasView.fxml"));
        Parent root = loader.load();

        // load canvas
        CanvasModel canvasModel = new CanvasModel();
        CanvasController canvasController = loader.getController();
        canvasController.initModel(canvasModel);

        Scene mainScene = new Scene(root, ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        stage.setScene(mainScene);
        stage.setTitle(APP_TITLE);
        stage.show();
    }

    public static void main(String[] args) {
        // configure log4j system
        BasicConfigurator.configure();

        // launch GUI
        launch(args);
    }
}
