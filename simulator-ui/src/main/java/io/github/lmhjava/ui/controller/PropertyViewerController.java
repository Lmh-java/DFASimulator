package io.github.lmhjava.ui.controller;

import io.github.lmhjava.ui.model.CanvasModel;

public class PropertyViewerController extends AppController {
    private CanvasModel canvasModel;

    public void initModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }
}
