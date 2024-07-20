package io.github.lmhjava.ui.controller;

import io.github.lmhjava.ui.object.CanvasComponent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * Controller that responses all draggable canvas components.
 * @link <a href="https://edencoding.com/drag-shapes-javafx/">Reference</a>
 */
public class DraggableCanvasComponentController {
    private final CanvasComponent target;
    private double anchorX;
    private double anchorY;
    private EventHandler<MouseEvent> setAnchor;
    private EventHandler<MouseEvent> updatePositionOnDrag;
    private EventHandler<MouseEvent> commitPositionOnRelease;
    private final int ACTIVE = 1;
    private final int INACTIVE = 0;
    private int cycleStatus = INACTIVE;
    private BooleanProperty isDraggable;

    public DraggableCanvasComponentController(CanvasComponent target) {
        this(target, false);
    }

    public DraggableCanvasComponentController(CanvasComponent target, boolean isDraggable) {
        this.target = target;
        createHandlers();
        createDraggableProperty();
        this.isDraggable.set(isDraggable);
    }

    private void createHandlers() {
        setAnchor = event -> {
            if (event.isPrimaryButtonDown()) {
                cycleStatus = ACTIVE;
                anchorX = event.getSceneX();
                anchorY = event.getSceneY();
            }
            if (event.isSecondaryButtonDown()) {
                cycleStatus = INACTIVE;
                target.setTranslateX(0);
                target.setTranslateY(0);
            }
        };
        updatePositionOnDrag = event -> {
            if (cycleStatus != INACTIVE) {
                target.setTranslateX(event.getSceneX() - anchorX);
                target.setTranslateY(event.getSceneY() - anchorY);
            }
        };
        commitPositionOnRelease = event -> {
            if (cycleStatus != INACTIVE) {
                // commit changes
                target.getXProperty().set((int) (target.getXProperty().get() + target.getTranslateX()));
                target.getYProperty().set((int) (target.getYProperty().get() + target.getTranslateY()));
                // clear changes from TranslateX and TranslateY
                target.setTranslateX(0);
                target.setTranslateY(0);
            }
        };
    }

    public void createDraggableProperty() {
        isDraggable = new SimpleBooleanProperty();
        isDraggable.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                target.addEventFilter(MouseEvent.MOUSE_PRESSED, setAnchor);
                target.addEventFilter(MouseEvent.MOUSE_DRAGGED, updatePositionOnDrag);
                target.addEventFilter(MouseEvent.MOUSE_RELEASED, commitPositionOnRelease);
            } else {
                target.removeEventFilter(MouseEvent.MOUSE_PRESSED, setAnchor);
                target.removeEventFilter(MouseEvent.MOUSE_DRAGGED, updatePositionOnDrag);
                target.removeEventFilter(MouseEvent.MOUSE_RELEASED, commitPositionOnRelease);
            }
        });
    }

    public boolean isIsDraggable() {
        return isDraggable.get();
    }

    public BooleanProperty isDraggableProperty() {
        return isDraggable;
    }
}
