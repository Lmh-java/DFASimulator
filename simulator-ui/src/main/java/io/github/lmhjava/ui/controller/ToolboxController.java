package io.github.lmhjava.ui.controller;

import io.github.lmhjava.engine.dfa.DFANode;
import io.github.lmhjava.engine.exception.NextNodeUndefException;
import io.github.lmhjava.ui.model.CanvasModel;
import io.github.lmhjava.ui.model.GlobalContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    private CanvasModel canvasModel;

    private static final String ERROR_FIELD_STYLE = "-fx-border-color: red;";
    private static final String SUCCESS_FIELD_STYLE = "-fx-border-color: green;";
    private static final String REJECTED_FIELD_STYLE = "-fx-border-color: orange;";

    // current symbol index in the parsed input sequence
    private int inputPos = 0;
    private String inputSignature = null;
    private List<String> inputSymbols = List.of();

    // timeline used for resume (step-through with delay)
    private Timeline resumeTimeline = null;

    /**
     * Initialize the canvas model
     *
     * @param canvasModel new canvas model
     */
    public void initModel(CanvasModel canvasModel) {
        this.canvasModel = canvasModel;
        pushInfo("Ready. Build a DFA, set an initial state, then run input.");
    }

    @FXML
    public void onAddNodeButtonClicked(ActionEvent unused) {
        final CanvasController canvasController = getCanvasController();
        if (canvasController == null) {
            pushError("Canvas is not ready yet.");
            return;
        }
        canvasController.addNodeAtVisibleCenter();
        pushInfo("Node added at viewport center.");
    }

    @FXML
    public void onAddEdgeButtonClicked(ActionEvent unused) {
        final CanvasController canvasController = getCanvasController();
        if (canvasController == null) {
            pushError("Canvas is not ready yet.");
            return;
        }
        canvasController.addEdge();
        pushInfo("Select a tail node, click Add Edge, then click a head node.");
    }

    @FXML
    public void onMoveButtonClicked(ActionEvent unused) {
        pushInfo("Move mode is always enabled: drag nodes and edge curves directly on canvas.");
    }

    @FXML
    public void onResumeDebuggerButtonClicked(ActionEvent unused) {
        if (!prepareSimulation()) {
            return;
        }

        // if already running, ignore
        if (resumeTimeline != null && resumeTimeline.getStatus() == Timeline.Status.RUNNING) {
            return;
        }

        resumeTimeline = new Timeline(new KeyFrame(Duration.millis(400), e -> {
            stepOnce();
        }));
        resumeTimeline.setCycleCount(Timeline.INDEFINITE);
        resumeTimeline.play();
        pushInfo("Running simulation...");
    }

    @FXML
    public void onStepOverDebuggerButtonClicked(ActionEvent unused) {
        if (!prepareSimulation()) {
            return;
        }
        stepOnce();
    }

    @FXML
    public void onStopDebuggerButtonClicked(ActionEvent unused) {
        inputSignature = null;
        inputSymbols = List.of();
        inputPos = 0;
        if (resumeTimeline != null) {
            resumeTimeline.stop();
            resumeTimeline = null;
        }
        if (canvasModel != null) {
            canvasModel.getDfaController().reset();
            canvasModel.setHighlightedComponent(null);
        }
        if (inputTextField != null) {
            inputTextField.setStyle("");
        }
        pushInfo("Simulation stopped.");
    }

    /**
     * Perform a single simulation step based on current input position.
     * This assumes input is processed character-by-character (each char is a symbol).
     */
    private void stepOnce() {
        if (canvasModel == null || inputSymbols.isEmpty()) {
            return;
        }

        if (inputPos >= inputSymbols.size()) {
            finishSimulation();
            return;
        }

        final String symbol = inputSymbols.get(inputPos);
        try {
            canvasModel.getDfaController().next(symbol);
            inputPos++;
            final DFANode current = canvasModel.getDfaController().getCurrentNode();
            pushInfo(String.format("Step %d/%d: read '%s' -> %s", inputPos, inputSymbols.size(), symbol, nodeName(current)));

            // if completed after this step, reflect final acceptance state
            if (inputPos >= inputSymbols.size()) {
                finishSimulation();
            }
        } catch (NextNodeUndefException e) {
            // highlight error and stop any running timeline
            inputTextField.setStyle(ERROR_FIELD_STYLE);
            if (resumeTimeline != null) {
                resumeTimeline.stop();
                resumeTimeline = null;
            }
            pushError(String.format("Transition undefined at step %d for symbol '%s'.", inputPos + 1, symbol));
        }
    }

    private boolean prepareSimulation() {
        if (canvasModel == null) {
            pushError("Canvas model is unavailable.");
            return false;
        }
        if (canvasModel.getDfaController().getInitialNode() == null) {
            pushError("Set one node as Initial State before running input.");
            inputTextField.setStyle(ERROR_FIELD_STYLE);
            return false;
        }

        final String text = inputTextField.getText();
        if (Strings.isBlank(text)) {
            inputTextField.setStyle(ERROR_FIELD_STYLE);
            pushError("Input is empty. Enter symbols to run.");
            return false;
        }

        final List<String> parsedSymbols = parseInputSymbols(text);
        if (parsedSymbols.isEmpty()) {
            inputTextField.setStyle(ERROR_FIELD_STYLE);
            pushError("Input contains no valid symbols.");
            return false;
        }

        if (!text.equals(inputSignature)) {
            inputSignature = text;
            inputSymbols = parsedSymbols;
            inputPos = 0;
            canvasModel.getDfaController().reset();
            canvasModel.setHighlightedComponent(null);
            pushInfo(String.format("Loaded %d symbols for simulation.", inputSymbols.size()));
        }

        inputTextField.setStyle("");
        return true;
    }

    private void finishSimulation() {
        if (resumeTimeline != null) {
            resumeTimeline.stop();
            resumeTimeline = null;
        }
        final boolean accepted = canvasModel.getDfaController().onAcceptState();
        inputTextField.setStyle(accepted ? SUCCESS_FIELD_STYLE : REJECTED_FIELD_STYLE);
        final DFANode current = canvasModel.getDfaController().getCurrentNode();
        if (accepted) {
            pushSuccess(String.format("Input accepted. Final state: %s", nodeName(current)));
        } else {
            pushInfo(String.format("Input rejected. Final state: %s", nodeName(current)));
        }
    }

    private List<String> parseInputSymbols(String rawInput) {
        final String trimmed = rawInput.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }
        if (trimmed.contains(",") || trimmed.contains(" ")) {
            return Arrays.stream(trimmed.split("[,\\s]+"))
                    .filter(token -> !token.isBlank())
                    .collect(Collectors.toList());
        }
        return trimmed.chars()
                .mapToObj(ch -> String.valueOf((char) ch))
                .collect(Collectors.toList());
    }

    private CanvasController getCanvasController() {
        final Object controller = GlobalContext.controllers.get("CanvasController");
        if (controller instanceof CanvasController canvasController) {
            return canvasController;
        }
        return null;
    }

    private MessageBarController getMessageBarController() {
        final Object controller = GlobalContext.controllers.get("MessageBarController");
        if (controller instanceof MessageBarController messageBarController) {
            return messageBarController;
        }
        return null;
    }

    private void pushInfo(String message) {
        final MessageBarController bar = getMessageBarController();
        if (bar != null) {
            bar.showInfo(message);
        }
    }

    private void pushSuccess(String message) {
        final MessageBarController bar = getMessageBarController();
        if (bar != null) {
            bar.showSuccess(message);
        }
    }

    private void pushError(String message) {
        final MessageBarController bar = getMessageBarController();
        if (bar != null) {
            bar.showError(message);
        }
    }

    private String nodeName(DFANode node) {
        if (node == null || Strings.isBlank(node.getContent())) {
            return "<unnamed-state>";
        }
        return node.getContent();
    }
}
