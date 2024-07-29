package io.github.lmhjava.ui.controller;

import io.github.lmhjava.ui.model.CanvasModel;
import io.github.lmhjava.ui.object.CanvasComponent;
import io.github.lmhjava.ui.object.DFAEdgeComponent;
import io.github.lmhjava.ui.object.DFANodeComponent;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

@Slf4j
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

    @FXML
    public ListView<String> dfaAlphabetSetField;

    @FXML
    public TextField newAlphabetField;

    private CanvasModel canvasModel;

    public void initModel(final CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
        canvasModel.getSelectedComponent().addListener((observable, oldValue, newValue) -> updateSubject(newValue, oldValue));
        propertyPane.getChildren().removeAll(edgeInfoPane, nodeInfoPane);

        // bind dfa information
        dfaAlphabetSetField.itemsProperty().set(FXCollections.observableArrayList(canvasModel.getDfaAlphabet().stream().toList()));
        canvasModel.getDfaAlphabet().addListener((SetChangeListener.Change<? extends String> c) -> {
            if (c.wasAdded()) {
                dfaAlphabetSetField.getItems().add(c.getElementAdded());
            } else if (c.wasRemoved()) {
                dfaAlphabetSetField.getItems().remove(c.getElementRemoved());
            }
        });
    }

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
            default -> {
            }
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
    }

    private void displayDFA() {
        propertyPane.getChildren().removeAll(edgeInfoPane, nodeInfoPane);
        if (!propertyPane.getChildren().contains(dfaInfoPane)) {
            propertyPane.getChildren().add(dfaInfoPane);
        }
    }

    public void addAlphabetElementToList(ActionEvent event) {
        if (Strings.isBlank(newAlphabetField.getText())) {
            newAlphabetField.setStyle("-fx-border-color: red");
        } else {
            newAlphabetField.setStyle(null);
            canvasModel.getDfaAlphabet().add(newAlphabetField.getText().trim());
            newAlphabetField.clear();
        }
    }
}
