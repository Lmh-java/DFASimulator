package io.github.lmhjava.ui.debug.controller;

import io.github.lmhjava.engine.dfa.DFAController;
import io.github.lmhjava.engine.dfa.DFAEdge;
import io.github.lmhjava.engine.dfa.DFANode;
import io.github.lmhjava.ui.controller.BaseAppController;
import io.github.lmhjava.ui.model.CanvasModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DebugController extends BaseAppController {
    private CanvasModel canvasModel;

    public void initModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

    @FXML
    public void onBackendDFAInfoClicked(ActionEvent unused) {
        final StringBuilder builder = new StringBuilder();
        final DFAController controller = canvasModel.getDfaController();
        builder.append("DFA INFO BELOW: \n");

        builder.append("Current Node = ");
        builder.append(controller.getCurrentNode());
        builder.append("\n");

        builder.append("Initial Node = ");
        builder.append(controller.getInitialNode());
        builder.append("\n");

        // print all nodes
        for (DFANode node : controller.getNodeSet()) {
            builder.append(node);
            builder.append("\n");
        }

        for (DFAEdge edge : controller.getEdgeSet()) {
            builder.append(edge);
            builder.append("\n");
        }

        builder.append("=========================");
        log.info(builder.toString());
    }
}
