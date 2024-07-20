package io.github.lmhjava.ui.controller;


import io.github.lmhjava.ui.model.CanvasModel;
import io.github.lmhjava.ui.object.CanvasComponent;
import io.github.lmhjava.ui.object.DFANodeComponent;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

/**
 * Canvas controller
 */
@Slf4j
public class CanvasController {

    private static final double SCROLL_THRESHOLD = 0.05;

    @FXML
    private ScrollPane scrollPane;

    /**
     * Canvas pane to render all the components
     */
    @FXML
    private Pane canvasPane;

    /**
     * canvas data model
     */
    private CanvasModel canvasModel;

    /**
     * Initialize the data model and the controller
     *
     * @param canvasModel data model to bind with this controller
     */
    public void initModel(CanvasModel canvasModel) {
        if (this.canvasModel != null) {
            log.warn("Canvas model is already set");
            throw new IllegalStateException("Canvas model is already set");
        }
        this.canvasModel = canvasModel;
        this.canvasModel.setCurrentSelection(null);
        canvasModel.getComponents().forEach((component) -> canvasPane.getChildren().add(component));

        // bind the scale factor
        this.canvasModel.getScale().bindBidirectional(canvasPane.scaleXProperty());
        this.canvasModel.getScale().bindBidirectional(canvasPane.scaleYProperty());

        // TODO: sync the actual displayed shapes with data models
        this.canvasModel.getComponents().addListener((SetChangeListener.Change<? extends CanvasComponent> c) -> {
            log.debug("Components in canvas model is changed, syncing to render");
            if (c.wasAdded()) {
                canvasPane.getChildren().add(c.getElementAdded());
            } else if (c.wasRemoved()) {
                canvasPane.getChildren().remove(c.getElementRemoved());
            }
        });

        initZoomFunction();
        initContextMenu();
    }

    /**
     * Initialize a context menu.
     *
     * @implNote open a context menu when right-clicked, and close it by left-clicking elsewhere.
     */
    private void initContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem("Add Node");
        menuItem.setOnAction((ActionEvent event) ->
                addNode(canvasModel.getContextMenuX().get(), canvasModel.getContextMenuY().get()));
        contextMenu.getItems().add(menuItem);

        canvasPane.setOnContextMenuRequested((ContextMenuEvent event) -> {
            log.debug("ContextMenu requested at [x={}, y={}]", event.getX(), event.getY());
            canvasModel.getContextMenuX().setValue(event.getX());
            canvasModel.getContextMenuY().setValue(event.getY());
            contextMenu.show(canvasPane.getParent(), event.getScreenX(), event.getScreenY());
            event.consume();
        });

        canvasPane.setOnMouseClicked((MouseEvent event) -> {
            if (contextMenu.isShowing() && event.getButton() != MouseButton.SECONDARY) {
                contextMenu.hide();
            }
            // reset selection when click else where
            if (event.getButton() == MouseButton.PRIMARY) {
                canvasModel.setCurrentSelection(null);
            }
        });
    }

    /**
     * Initialize zoom function of the canvas pane.
     *
     * @implNote User can pinch or scroll to zoom the canvas. Zooming is always pivot to the center of the canvas.
     * @link <a href="https://makemeengr.com/javafx-8-zooming-relative-to-mouse-pointer/#google_vignette">Reference</a>
     */
    private void initZoomFunction() {
        scrollPane.setOnZoom(e -> {
            log.debug("Zoom event at {} {}", e.getX(), e.getY());

            Bounds viewPort = scrollPane.getViewportBounds();
            Bounds contentSize = canvasPane.getBoundsInParent();
            final double centerPosX = (contentSize.getWidth() - viewPort.getWidth()) * scrollPane.getHvalue() + viewPort.getWidth() / 2;
            final double centerPosY = (contentSize.getHeight() - viewPort.getHeight()) * scrollPane.getVvalue() + viewPort.getHeight() / 2;

            canvasPane.setScaleX(canvasPane.getScaleX() * e.getZoomFactor());
            canvasPane.setScaleY(canvasPane.getScaleY() * e.getZoomFactor());

            final double newCenterX = centerPosX * e.getZoomFactor();
            final double newCenterY = centerPosY * e.getZoomFactor();

            scrollPane.setHvalue((newCenterX - viewPort.getWidth() / 2) / (contentSize.getWidth() * e.getZoomFactor() - viewPort.getWidth()));
            scrollPane.setVvalue((newCenterY - viewPort.getHeight() / 2) / (contentSize.getHeight() * e.getZoomFactor() - viewPort.getHeight()));

        });
    }

    /**
     * Add a node to the DFA
     *
     * @param x x position of the node
     * @param y y position of the node
     */
    private void addNode(int x, int y) {
        // Delegate click event handler to every node object
        DFANodeComponent node = new DFANodeComponent(x, y);
        node.setOnMouseClicked((MouseEvent event) -> {
            this.canvasModel.setCurrentSelection(node);
            event.consume();
        });
        // make the node draggable
        DraggableCanvasComponentController draggable = new DraggableCanvasComponentController(node, true, canvasModel);
        draggable.createDraggableProperty();
        canvasModel.getComponents().add(node);
        log.debug("Add a node at [x = {}, y = {}]", x, y);
    }

}
