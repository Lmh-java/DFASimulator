package io.github.lmhjava.ui.controller;

import io.github.lmhjava.ui.model.CanvasModel;
import io.github.lmhjava.ui.object.CanvasComponent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * Controller that responses all draggable canvas components.
 * @link <a href="https://edencoding.com/drag-shapes-javafx/">Reference</a>
 *
 * TODO: refactor drag controllers
 */
public class DraggableCanvasComponentController {
    protected final CanvasComponent target;
    protected double anchorX;
    protected double anchorY;
    protected EventHandler<MouseEvent> setAnchor;
    protected EventHandler<MouseEvent> updatePositionOnDrag;
    protected EventHandler<MouseEvent> commitPositionOnRelease;
    protected final int ACTIVE = 1;
    protected final int INACTIVE = 0;
    protected int cycleStatus = INACTIVE;
    protected BooleanProperty isDraggable;
    protected final CanvasModel canvasModel;

    public DraggableCanvasComponentController(CanvasComponent target, CanvasModel canvasModel) {
        this(target, false, canvasModel);
    }

    public DraggableCanvasComponentController(CanvasComponent target, boolean isDraggable, CanvasModel canvasModel) {
        this.target = target;
        createHandlers();
        createDraggableProperty();
        this.isDraggable.set(isDraggable);
        this.canvasModel = canvasModel;
    }

    protected void createHandlers() {
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
                target.setTranslateX((event.getSceneX() - anchorX) / canvasModel.getScale().get());
                target.setTranslateY((event.getSceneY() - anchorY) / canvasModel.getScale().get());
                target.setOpacity(0.5);
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
                target.setOpacity(1);
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
