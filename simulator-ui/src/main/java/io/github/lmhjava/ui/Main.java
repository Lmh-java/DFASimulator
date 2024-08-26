package io.github.lmhjava.ui;

import io.github.lmhjava.ui.controller.AppController;
import io.github.lmhjava.ui.controller.CanvasController;
import io.github.lmhjava.ui.controller.PropertyViewerController;
import io.github.lmhjava.ui.controller.ToolboxController;
import io.github.lmhjava.ui.model.CanvasModel;
import io.github.lmhjava.ui.model.GlobalContext;
import io.github.lmhjava.ui.util.ScreenUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.BasicConfigurator;

/**
 * Main entrance of application
 */
@Slf4j
public class Main extends Application {

    private final static String APP_TITLE = "DFA Simulator";

    /**
     * Turn this debug flag to true to get access to debug features
     */
    private final static boolean DEBUG_MODE = false;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AppView.fxml"));
        Parent root = loader.load();

        final AppController appController = loader.getController();
        log.debug("Initialized {} controllers : {}", GlobalContext.controllers.size(), GlobalContext.controllers);

        // load canvas
        final CanvasModel canvasModel = new CanvasModel();
        final CanvasController canvasController = (CanvasController) GlobalContext.controllers.get("CanvasController");
        canvasController.initModel(canvasModel);

        // load properties view
        final PropertyViewerController propertyViewerController = (PropertyViewerController) GlobalContext.controllers.get("PropertyViewerController");
        propertyViewerController.initModel(canvasModel);

        // load tool box view
        final ToolboxController toolboxController = (ToolboxController) GlobalContext.controllers.get("ToolboxController");
        toolboxController.initModel(canvasModel);

        Scene mainScene = new Scene(root, ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        stage.setScene(mainScene);
        stage.setTitle(APP_TITLE);
        stage.show();
    }

    public static void main(String[] args) {
        // configure log4j system
        BasicConfigurator.configure();

        // launch GUI
        log.debug("Application started");
        launch(args);
    }
}
