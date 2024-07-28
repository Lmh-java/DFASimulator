package io.github.lmhjava.ui.controller;

import io.github.lmhjava.ui.model.CanvasModel;
import io.github.lmhjava.ui.object.CanvasComponent;
import io.github.lmhjava.ui.object.DFAEdgeComponent;
import io.github.lmhjava.ui.object.DFANodeComponent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PropertyViewerController extends AppController {
    @FXML
    public Region iconRegion;

    @FXML
    public GridPane edgeInfoPane;

    @FXML
    public GridPane nodeInfoPane;

    @FXML
    public GridPane dfaInfoPane;

    @FXML
    public ListView<String> edgeAlphabetSetField;

    @FXML
    public CheckBox edgeIsElseEdgeCheckBox;

    @FXML
    public Label edgeIsSelfLoopLabel;

    @FXML
    public TextField nodeContentField;

    @FXML
    public Label nodeIsCompleteLabel;

    @FXML
    public CheckBox nodeIsInitialCheckBox;

    @FXML
    public CheckBox nodeIsAcceptedCheckBox;

    @FXML
    public VBox propertyPane;

    private CanvasModel canvasModel;

    public void initModel(final CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
        canvasModel.getSelectedComponent().addListener((observable, oldValue, newValue) -> updateSubject(newValue, oldValue));
        propertyPane.getChildren().removeAll(edgeInfoPane, nodeInfoPane);
    }

    public void updateSubject(final CanvasComponent component, final CanvasComponent previousComponent) {
        if (previousComponent instanceof DFANodeComponent node) {
            cleanUpNode(node);
        } else if (previousComponent instanceof DFAEdgeComponent edge) {
            cleanUpEdge(edge);
        }

        if (component instanceof DFANodeComponent node) {
            displayNode(node);
        } else if (component instanceof DFAEdgeComponent edge) {
            displayEdge(edge);
        } else if (component == null) {
            displayDFA();
        }
    }

    private void cleanUpNode(final DFANodeComponent previousNode) {
        previousNode.getContentProperty().unbind();
        previousNode.getIsAcceptProperty().unbind();
    }

    private void cleanUpEdge(final DFAEdgeComponent previousEdge) {

    }

    private void displayNode(final DFANodeComponent node) {
        propertyPane.getChildren().removeAll(dfaInfoPane, edgeInfoPane);
        if (!propertyPane.getChildren().contains(nodeInfoPane)) {
            propertyPane.getChildren().add(nodeInfoPane);
        }
        nodeContentField.setText(node.getContentProperty().get());
        node.getContentProperty().bind(nodeContentField.textProperty());

        nodeIsAcceptedCheckBox.setSelected(node.getIsAcceptProperty().get());
        node.getIsAcceptProperty().bind(nodeIsAcceptedCheckBox.selectedProperty());
    }

    private void displayEdge(final DFAEdgeComponent edge) {
        propertyPane.getChildren().removeAll(dfaInfoPane, nodeInfoPane);
        if (!propertyPane.getChildren().contains(edgeInfoPane)) {
            propertyPane.getChildren().add(edgeInfoPane);
        }
    }

    private void displayDFA() {
        propertyPane.getChildren().removeAll(edgeInfoPane, nodeInfoPane);
        if (!propertyPane.getChildren().contains(dfaInfoPane)) {
            propertyPane.getChildren().add(dfaInfoPane);
        }
    }
}
