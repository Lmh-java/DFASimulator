package io.github.lmhjava.ui.controller;

import io.github.lmhjava.ui.model.CanvasModel;
import io.github.lmhjava.ui.object.CanvasComponent;

import java.util.function.BiConsumer;

public class DragToAdjustComponentController extends DraggableCanvasComponentController {

    private final BiConsumer<Double, Double> dragValueConsumer;

    public DragToAdjustComponentController(CanvasComponent target, boolean isDraggable, CanvasModel canvasModel, BiConsumer<Double, Double> dragValueConsumer) {
        super(target, isDraggable, canvasModel);
        this.dragValueConsumer = dragValueConsumer;
    }

    @Override
    public void createHandlers() {
        setAnchor = event -> {
            if (event.isPrimaryButtonDown()) {
                cycleStatus = ACTIVE;
                anchorX = event.getSceneX();
                anchorY = event.getSceneY();
            }
            if (event.isSecondaryButtonDown()) {
                cycleStatus = INACTIVE;
            }
        };
        updatePositionOnDrag = event -> {
            if (cycleStatus != INACTIVE) {
                dragValueConsumer.accept((event.getSceneX() - anchorX) / canvasModel.getScale().get(), (event.getSceneY() - anchorY) / canvasModel.getScale().get());
                target.setOpacity(0.5);
            }
        };
        commitPositionOnRelease = event -> {
            target.setOpacity(1);
        };
    }


}
