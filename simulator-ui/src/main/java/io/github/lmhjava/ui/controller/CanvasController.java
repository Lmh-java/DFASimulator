package io.github.lmhjava.ui.controller;


import io.github.lmhjava.ui.model.CanvasModel;
import io.github.lmhjava.ui.object.CanvasComponent;
import io.github.lmhjava.ui.object.DFAEdgeComponent;
import io.github.lmhjava.ui.object.DFANodeComponent;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

/**
 * Canvas controller
 */
@Slf4j
public class CanvasController extends BaseAppController {

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

    private ContextMenu contextMenu;

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

        this.canvasModel.getComponents().addListener((SetChangeListener.Change<? extends CanvasComponent> c) -> {
            log.debug("Components in canvas model is changed, syncing to render");
            if (c.wasAdded()) {
                canvasPane.getChildren().add(c.getElementAdded());
            } else if (c.wasRemoved()) {
                canvasPane.getChildren().remove(c.getElementRemoved());
            }
        });

        initZoomFunction();
        initDefaultContextMenu();
        initMouseListeners();
        initKeyboardListeners();
    }

    /**
     * Initialize a default context menu. A default context menu opens when right-clicking the blank canvas area.
     *
     * @implNote open a context menu when right-clicked canvas, and close it by left-clicking elsewhere.
     */
    private void initDefaultContextMenu() {
        contextMenu = new ContextMenu();
        MenuItem addNodeItem = new MenuItem("Add Node");
        addNodeItem.setOnAction((ActionEvent event) ->
                addNode(canvasModel.getContextMenuX().get(), canvasModel.getContextMenuY().get()));

        canvasPane.setOnContextMenuRequested((ContextMenuEvent event) -> {
            log.debug("Default contextMenu requested at [x={}, y={}]", event.getX(), event.getY());
            canvasModel.getContextMenuX().setValue(event.getX());
            canvasModel.getContextMenuY().setValue(event.getY());
            contextMenu.show(canvasPane.getParent(), event.getScreenX(), event.getScreenY());
            event.consume();
        });

        contextMenu.getItems().add(addNodeItem);
    }

    /**
     * Initialize mouse event listeners and handlers
     */
    private void initMouseListeners() {
        canvasPane.setOnMouseClicked((MouseEvent event) -> {
            if (contextMenu.isShowing() && event.getButton() != MouseButton.SECONDARY) {
                contextMenu.hide();
                event.consume();
            }
            // reset selection when click else where
            if (event.getButton() == MouseButton.PRIMARY) {
                canvasModel.setCurrentSelection(null);
                event.consume();
            }
        });
    }

    /**
     * Initialize all keyboard listeners
     */
    private void initKeyboardListeners() {
        scrollPane.setOnKeyPressed((KeyEvent event) -> {
            log.debug("Keyboard Event ({}) detected", event.getCode());
            // delete a component
            if (event.getCode() == KeyCode.BACK_SPACE) {
                if (canvasModel.getCurrentSelection() != null) {
                    removeComponent(canvasModel.getCurrentSelection());
                }
            }
            event.consume();
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
            log.debug("Zoom event at [x={} y={}]", e.getX(), e.getY());

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
    public void addNode(int x, int y) {
        // Delegate click event handler to every node object
        final DFANodeComponent node = new DFANodeComponent(x, y);
        final ContextMenu nodeMenu = getNodeContextMenu(node);
        node.setOnMouseClicked((MouseEvent event) -> {
            // select current node
            this.canvasModel.setCurrentSelection(node);
            event.consume();
        });
        // show the context menu for a node
        node.setOnContextMenuRequested((ContextMenuEvent event) -> {
            nodeMenu.show(node, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        // make the node draggable
        new DraggableCanvasComponentController(node, true, canvasModel);
        canvasModel.getDfaController().registerNode(node.getNode());
        node.getNode().setOnCurrentStateUpdate(() -> canvasModel.setHighlightedComponent(node));
        canvasModel.getComponents().add(node);
        log.debug("Add a node at [x={}, y={}]", x, y);
    }

    /**
     * Add an edge to the canvas
     */
    public void addEdge() {
        if (canvasModel.getCurrentSelection() instanceof DFANodeComponent tailNode) {
            final DFAEdgeComponent edge = new DFAEdgeComponent(tailNode);
            edge.initShape(canvasPane);
            canvasModel.getComponents().add(edge);
            canvasModel.setCurrentSelection(null);

            // commit when the current selection is changed
            final ChangeListener<? super CanvasComponent> selectionChangeListener = new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends CanvasComponent> ob, CanvasComponent oldValue, CanvasComponent newValue) {
                    if (newValue instanceof DFANodeComponent headNode) {
                        final ContextMenu edgeMenu = getEdgeContextMenu(edge);
                        edge.settle(headNode, canvasPane);
                        // allows user to bend the arrow
                        if (!edge.isSelfLoop()) {
                            new DragToAdjustComponentController(edge, true, canvasModel, (deltaX, deltaY) -> {
                                if (edge.isVertical()) {
                                    edge.setArrowControl(deltaX * (edge.isUpToDown() ? -1 : 1));
                                } else {
                                    edge.setArrowControl(deltaY * (!edge.isLeftToRight() ? -1 : 1));
                                }
                            });
                        }
                        // mouse click listener
                        edge.setOnMouseClicked((MouseEvent event) -> {
                            canvasModel.setCurrentSelection(edge);
                            event.consume();
                        });
                        // context menu request listener
                        edge.setContextMenu((ContextMenuEvent event) -> {
                            edgeMenu.show(edge, event.getScreenX(), event.getScreenY());
                            event.consume();
                        });
                        // remove this listener
                        canvasModel.getSelectedComponent().removeListener(this);
                        canvasModel.getDfaController().registerEdge(edge.getEdge());
                        initKeyboardListeners();
                    }
                }
            };
            canvasModel.getSelectedComponent().addListener(selectionChangeListener);

            // or ESC is pressed to stop adding an edge
            final EventHandler<? super KeyEvent> keyEventHandler = (EventHandler<KeyEvent>) event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    removeComponent(edge);
                    canvasModel.getSelectedComponent().removeListener(selectionChangeListener);
                    // remove the current listener and resume the original listener
                    initKeyboardListeners();
                }
                event.consume();
            };
            scrollPane.setOnKeyPressed(keyEventHandler);
        }

    }

    /**
     * Constructs a context menu for a node.
     *
     * @param node the node this menu for
     * @return a context menu that shows for a node
     */
    private ContextMenu getNodeContextMenu(DFANodeComponent node) {
        final ContextMenu nodeMenu = new ContextMenu();
        // delete the current node
        final MenuItem deleteNodeItem = new MenuItem("Delete Node");
        deleteNodeItem.setOnAction((ActionEvent event) -> removeComponent(node));

        // add an edge to the current node
        final MenuItem addEdgeItem = new MenuItem("Add Edge");
        addEdgeItem.setOnAction((ActionEvent event) -> addEdge());
        nodeMenu.getItems().addAll(deleteNodeItem, new SeparatorMenuItem(), addEdgeItem);
        return nodeMenu;
    }

    /**
     * Constructs a context menu for an edge.
     *
     * @param edge the edge this menu for
     * @return a context menu that shows for an edge
     */
    private ContextMenu getEdgeContextMenu(DFAEdgeComponent edge) {
        final ContextMenu edgeMenu = new ContextMenu();
        // delete current edge
        final MenuItem deleteEdgeItem = new MenuItem("Delete Edge");
        deleteEdgeItem.setOnAction((ActionEvent event) -> removeComponent(edge));
        edgeMenu.getItems().add(deleteEdgeItem);
        return edgeMenu;
    }

    /**
     * Remove any canvas component being rendered on canvas
     *
     * @param component canvas component
     */
    private void removeComponent(CanvasComponent component) {
        assert canvasModel.getComponents().contains(component);
        canvasModel.getComponents().remove(component);
        // if user deletes a node, also delete relevant edges.
        // NOTE: run this task in the background to save resources
        if (component instanceof DFANodeComponent c) {
            Platform.runLater(() -> canvasModel.getComponents().removeIf((CanvasComponent comp) -> comp instanceof DFAEdgeComponent edge && (edge.getTailNode() == c || edge.getHeadNode() == c)));
        }
        // if the instance being deleted is selected, set the current selection to null
        if (component == canvasModel.getCurrentSelection()) {
            canvasModel.setCurrentSelection(null);
        }
    }
}
