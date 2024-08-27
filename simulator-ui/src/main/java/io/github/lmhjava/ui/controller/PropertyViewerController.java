package io.github.lmhjava.ui.controller;

import io.github.lmhjava.ui.model.CanvasModel;
import io.github.lmhjava.ui.object.CanvasComponent;
import io.github.lmhjava.ui.object.DFAEdgeComponent;
import io.github.lmhjava.ui.object.DFANodeComponent;
import io.github.lmhjava.ui.util.EnhancedBindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

@Slf4j
public class PropertyViewerController extends BaseAppController {
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

    @FXML
    public ListView<String> dfaAlphabetSetField;

    @FXML
    public TextField newAlphabetField;

    private CanvasModel canvasModel;

    /**
     * initialize canvas model
     *
     * @param canvasModel canvas model
     */
    public void initModel(final CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
        canvasModel.getSelectedComponent().addListener((observable, oldValue, newValue) -> updateSubject(newValue, oldValue));
        propertyPane.getChildren().removeAll(edgeInfoPane, nodeInfoPane);
        displayDFA();

        // bind dfa information
        dfaAlphabetSetField.itemsProperty().set(FXCollections.observableArrayList(canvasModel.getDfaAlphabet().stream().toList()));
        EnhancedBindings.bindContent(dfaAlphabetSetField.getItems(), canvasModel.getDfaAlphabet());

        // optimize user interaction: user can quickly add new alphabet element without moving mouse
        newAlphabetField.setOnKeyPressed(event -> {
            if (newAlphabetField.isFocused() && event.getCode() == KeyCode.ENTER) {
                addAlphabetElementToList();
                newAlphabetField.requestFocus();
            }
        });
    }

    /**
     * Updates the subject whose property is shown
     *
     * @param component subject
     * @param previousComponent previous subject
     */
    public void updateSubject(final CanvasComponent component, final CanvasComponent previousComponent) {
        if (previousComponent instanceof DFANodeComponent node) {
            cleanUpNode(node);
        } else if (previousComponent instanceof DFAEdgeComponent edge) {
            cleanUpEdge(edge);
        }

        switch (component) {
            case DFANodeComponent node -> displayNode(node);
            case DFAEdgeComponent edge -> displayEdge(edge);
            case null -> displayDFA();
            default -> {}
        }
    }

    /**
     * Disconnects all the bindings related to the previous node.
     *
     * @param previousNode previous node component
     */
    private void cleanUpNode(final DFANodeComponent previousNode) {
        previousNode.getContentProperty().unbind();
        previousNode.getIsAcceptProperty().unbind();
        nodeIsAcceptedCheckBox.setOnMouseClicked(event -> {});
    }

    /**
     * Disconnects all the bindings related to the previous edge.
     *
     * @param previousEdge previous edge.
     */
    private void cleanUpEdge(final DFAEdgeComponent previousEdge) {
        previousEdge.getIsElseProperty().unbind();
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

        nodeIsInitialCheckBox.setSelected(node.getNode() != null && node.getNode().equals(canvasModel.getDfaController().getInitialNode()));
        nodeIsInitialCheckBox.setOnMouseClicked((mouseEvent -> canvasModel.setInitialNode(nodeIsInitialCheckBox.isSelected() ? node : null)));
    }

    private void displayEdge(final DFAEdgeComponent edge) {
        propertyPane.getChildren().removeAll(dfaInfoPane, nodeInfoPane);
        if (!propertyPane.getChildren().contains(edgeInfoPane)) {
            propertyPane.getChildren().add(edgeInfoPane);
        }
        // sync the dfa list for user to select
        edgeAlphabetSetField.itemsProperty().set(dfaAlphabetSetField.getItems());
        edgeAlphabetSetField.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                final ListCell<String> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(String s, boolean empty) {
                        super.updateItem(s, empty);
                        if (empty || s == null) {
                            setText(null);
                            setStyle(null);
                        } else {
                            setText(s);
                            if (edge.getAlphabets().contains(s)) {
                                setStyle("-fx-background-color: rgba(0,128,0,0.5);");
                            } else {
                                setStyle(null);
                            }
                        }
                    }
                };

                cell.setOnMouseClicked(event -> {
                    if (cell.getText() != null) {
                        if (edge.getAlphabets().contains(cell.getText())) {
                            edge.getAlphabets().remove(cell.getText());
                            cell.setStyle(null);
                        } else {
                            edge.getAlphabets().add(cell.getText());
                            cell.setStyle("-fx-background-color: rgba(0,128,0,0.5);");
                        }
                    }
                    edgeAlphabetSetField.getSelectionModel().clearSelection();
                });

                return cell;
            }
        });

        edgeIsElseEdgeCheckBox.setSelected(edge.getIsElseProperty().get());
        edge.getIsElseProperty().bind(edgeIsElseEdgeCheckBox.selectedProperty());
    }

    private void displayDFA() {
        propertyPane.getChildren().removeAll(edgeInfoPane, nodeInfoPane);
        if (!propertyPane.getChildren().contains(dfaInfoPane)) {
            propertyPane.getChildren().add(dfaInfoPane);
        }
    }

    public void addAlphabetElementToList() {
        if (Strings.isBlank(newAlphabetField.getText())) {
            newAlphabetField.setStyle("-fx-border-color: red");
        } else {
            newAlphabetField.setStyle(null);
            canvasModel.getDfaAlphabet().add(newAlphabetField.getText().trim());
            newAlphabetField.clear();
        }
    }
}
