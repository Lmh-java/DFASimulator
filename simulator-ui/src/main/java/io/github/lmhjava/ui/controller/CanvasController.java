package io.github.lmhjava.ui.controller;


import io.github.lmhjava.ui.model.CanvasModel;
import io.github.lmhjava.ui.object.CanvasComponent;
import io.github.lmhjava.ui.object.DFAEdgeComponent;
import io.github.lmhjava.ui.object.DFANodeComponent;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import lombok.extern.slf4j.Slf4j;

/**
 * Canvas controller
 */
@Slf4j
public class CanvasController {

    /**
     * Default radius to use when rendering the node
     */
    private static final int NODE_CIRCLE_RADIUS = 40;
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
        canvasModel.getComponents().forEach((component) -> canvasPane.getChildren().add(getRenderObj(component)));

        // TODO: sync the actual displayed shapes with data models
        this.canvasModel.getComponents().addListener((SetChangeListener.Change<? extends CanvasComponent> c) -> {
            log.debug("Components in canvas model is changed, syncing to render");
            if (c.wasAdded()) {
                canvasPane.getChildren().add(getRenderObj(c.getElementAdded()));
            } else if (c.wasRemoved()) {
                canvasPane.getChildren().remove(getRenderObj(c.getElementRemoved()));
            }
            // update the canvas and zoom to the center
            scrollPane.layout();
            scrollPane.setHvalue(0.5);
            scrollPane.setVvalue(0.5);
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
        });
    }

    /**
     * Initialize zoom function of the canvas pane.
     *
     * @implNote User can pinch or scroll to zoom the canvas. Zooming is always around the center of the canvas.
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

            final double denoX = contentSize.getWidth() * e.getZoomFactor() - viewPort.getWidth();
            final double denoY = contentSize.getHeight() * e.getZoomFactor() - viewPort.getHeight();
            if (denoX >= SCROLL_THRESHOLD) {
                scrollPane.setHvalue((newCenterX - viewPort.getWidth() / 2) / denoX);
            }
            if (denoY >= SCROLL_THRESHOLD) {
                scrollPane.setVvalue((newCenterY - viewPort.getHeight() / 2) / denoY);
            }
        });
    }

    /**
     * Returns the render object based on the provided {@code CanvasComponent}
     *
     * @param component canvas component
     * @return node to be rendered
     */
    private Node getRenderObj(CanvasComponent component) {
        if (component instanceof DFANodeComponent c) {
            return getNodeRenderObj(c);
        } else if (component instanceof DFAEdgeComponent c) {
            return getEdgeRenderObj(c);
        }
        log.warn("Unknown component type: {}", component.getClass().getName());
        throw new IllegalArgumentException("Unknown component type: " + component.getClass().getSimpleName());
    }

    /**
     * Returns the renderable object of a DFA node.
     *
     * @param node a DFA node component
     * @return a shape representing a node to be rendered
     */
    private Circle getNodeRenderObj(DFANodeComponent node) {
        Circle circle = new Circle();
        circle.centerXProperty().bindBidirectional(node.getXProperty());
        circle.centerYProperty().bindBidirectional(node.getYProperty());
        circle.setRadius(NODE_CIRCLE_RADIUS);
        return circle;
    }

    /**
     * Returns the renderable object of a DFA edge.
     *
     * @param edge a DFA edge component
     * @return a shape representing a edge to be rendered
     */
    private Shape getEdgeRenderObj(DFAEdgeComponent edge) {
        // TODO: complete the edge rendering process
        return null;
    }

    /**
     * Add a node to the DFA
     *
     * @param x x position of the node
     * @param y y position of the node
     */
    private void addNode(int x, int y) {
        DFANodeComponent node = new DFANodeComponent();
        node.getXProperty().set(x);
        node.getYProperty().set(y);
        canvasModel.getComponents().add(node);
        log.debug("Add a node at [x = {}, y = {}]", x, y);
    }

}
