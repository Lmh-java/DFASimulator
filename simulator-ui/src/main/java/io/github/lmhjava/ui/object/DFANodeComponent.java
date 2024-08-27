package io.github.lmhjava.ui.object;

import io.github.lmhjava.engine.dfa.DFANode;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import lombok.Getter;


/**
 * Renderable wrapper of a DFA node.
 */
public class DFANodeComponent extends CanvasComponent {

    /**
     * Default radius to use when rendering the node
     */
    public static final int NODE_CIRCLE_RADIUS = 40;
    public static final int SELECTION_CIRCLE_THICKNESS = 10;
    private static final int NODE_CIRCLE_STROKE_WIDTH = 5;
    private static final int SELECTION_CIRCLE_STROKE_WIDTH = 2;
    private static final Paint NODE_CIRCLE_STROKE = Color.BLACK;
    private static final Paint INITIAL_NODE_CIRCLE_STROKE = Color.GREEN;

    @Getter
    private final DFANode node;
    private final Circle circle;
    private final Label label;
    private final Circle surroundingCircle;
    private final StackPane pane;

    @Getter
    private final StringProperty contentProperty;
    @Getter
    private final BooleanProperty isInitialProperty;
    @Getter
    private final BooleanProperty isAcceptProperty;

    @Getter
    private final NumberBinding centerXProperty;
    @Getter
    private final NumberBinding centerYProperty;

    public DFANodeComponent(int x, int y) {
        super(x, y);
        node = new DFANode("Default");
        pane = new StackPane();
        circle = new Circle();
        surroundingCircle = new Circle();
        label = new Label();

        contentProperty = new SimpleStringProperty(node.getContent());
        isInitialProperty = new SimpleBooleanProperty(false);
        isAcceptProperty = new SimpleBooleanProperty(false);

        centerXProperty = getXProperty().add(NODE_CIRCLE_RADIUS + SELECTION_CIRCLE_THICKNESS);
        centerYProperty = getYProperty().add(NODE_CIRCLE_RADIUS + SELECTION_CIRCLE_THICKNESS);

        initShape();
        initPropertyListeners();
    }

    private void initShape() {
        circle.setRadius(NODE_CIRCLE_RADIUS);
        circle.setStroke(NODE_CIRCLE_STROKE);
        circle.setStrokeWidth(NODE_CIRCLE_STROKE_WIDTH);

        surroundingCircle.setRadius(NODE_CIRCLE_RADIUS + SELECTION_CIRCLE_THICKNESS);
        surroundingCircle.setFill(null);
        surroundingCircle.setStroke(NODE_CIRCLE_STROKE);
        surroundingCircle.setStrokeWidth(SELECTION_CIRCLE_STROKE_WIDTH);
        surroundingCircle.setVisible(false);

        label.textProperty().bind(contentProperty);
        label.setStyle("-fx-text-fill: white;");

        pane.getChildren().addAll(circle, surroundingCircle, label);
        super.getChildren().add(pane);

        this.setCursor(Cursor.HAND);
    }

    private void initPropertyListeners() {
        contentProperty.addListener((observable, oldValue, newValue) -> node.setContent(newValue));
        isAcceptProperty.addListener((observable, oldValue, newValue) -> {
            node.setAccepted(newValue);
            surroundingCircle.setVisible(newValue);
        });
        isInitialProperty.addListener((observable, oldValue, newValue) -> {
           if (newValue) {
               circle.setFill(INITIAL_NODE_CIRCLE_STROKE);
               circle.setStroke(INITIAL_NODE_CIRCLE_STROKE);
               surroundingCircle.setStroke(INITIAL_NODE_CIRCLE_STROKE);
           } else {
               circle.setFill(NODE_CIRCLE_STROKE);
               circle.setStroke(NODE_CIRCLE_STROKE);
               surroundingCircle.setStroke(NODE_CIRCLE_STROKE);
           }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void notifySelected() {
        this.setEffect(new Glow(1.0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyUnselected() {
        this.setEffect(null);
    }

    @Override
    public void sync() {
        contentProperty.set(node.getContent());
        isAcceptProperty.set(node.isAccepted());
    }

    @Override
    public void onHighlight() {
        circle.setFill(Color.YELLOW);
    }

    @Override
    public void deHighlight() {
        circle.setFill(Color.BLACK);
    }
}
