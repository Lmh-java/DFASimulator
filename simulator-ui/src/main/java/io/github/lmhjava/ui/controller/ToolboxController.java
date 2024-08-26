package io.github.lmhjava.ui.controller;

import io.github.lmhjava.engine.dfa.DFAController;
import io.github.lmhjava.engine.dfa.DFAEdge;
import io.github.lmhjava.engine.dfa.DFANode;
import io.github.lmhjava.engine.exception.NextNodeUndefException;
import io.github.lmhjava.ui.model.CanvasModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.util.Strings;

public class ToolboxController extends BaseAppController {

    @FXML
    private TextField inputTextField;

    @FXML
    private Button stopDebuggerButton;

    @FXML
    private Button resumeDebuggerButton;

    @FXML
    private Button stepOverDebuggerButton;

    @FXML
    private Button addNodeButton;

    @FXML
    private Button addEdgeButton;

    @FXML
    private Button moveButton;

    @FXML
    private Button selectButton;

    /*
    Here we use the DFA system we designed to control the debugger among different states (start, stop, etc.)
     */
    private DFAController debuggerController;

    private CanvasModel canvasModel;

    /**
     * Initialize the canvas model
     *
     * @param canvasModel new canvas model
     */
    public void initModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
    }

    /**
     * Initialize the debugger state machine.
     * This state machine only has two states (on, off), transits by debugger button clicks.
     */
    public ToolboxController() {
        final DFANode onNode = new DFANode("on");
        final DFANode offNode = new DFANode("off");
        final DFAEdge onToOff = new DFAEdge(onNode, offNode);
        onToOff.registerAllAlphabet("resume", "manualOff");

        final DFAEdge offToOn = new DFAEdge(offNode, onNode);
        offToOn.registerAlphabet("step");
    }

    @FXML
    public void onAddNodeButtonClicked(ActionEvent unused) {

    }

    @FXML
    public void onAddEdgeButtonClicked(ActionEvent unused) {

    }

    @FXML
    public void onMoveButtonClicked(ActionEvent unused) {

    }

    @FXML
    public void onResumeDebuggerButtonClicked(ActionEvent unused) {

    }

    @FXML
    public void onStepOverDebuggerButtonClicked(ActionEvent unused) {
        // check validity
        if (Strings.isBlank(inputTextField.getText())) {
            // TODO: set the boarder to red
        }

        try {
            debuggerController.next("step");
        } catch (NextNodeUndefException e) {
            // convert to runtime exception
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onStopDebuggerButtonClicked(ActionEvent unused) {

    }
}
