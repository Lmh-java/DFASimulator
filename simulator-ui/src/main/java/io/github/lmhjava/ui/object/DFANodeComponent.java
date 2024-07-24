package io.github.lmhjava.ui.object;

import io.github.lmhjava.engine.node.DFANode;
import javafx.beans.binding.NumberBinding;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
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

    @Getter
    private final DFANode node;
    private final Circle circle;
    private final Label label;
    private final Circle selectionCircle;
    private final StackPane pane;

    @Getter
    private final NumberBinding centerXProperty;
    @Getter
    private final NumberBinding centerYProperty;

    public DFANodeComponent(int x, int y) {
        super(x, y);
        node = new DFANode("Default");
        pane = new StackPane();
        // FIXME: delete the following line of test code
        pane.setStyle("-fx-background-color: rgba(0, 0, 255, 0.4);");
        circle = new Circle();
        selectionCircle = new Circle();
        label = new Label();

        centerXProperty = getXProperty().add(NODE_CIRCLE_RADIUS + SELECTION_CIRCLE_THICKNESS);
        centerYProperty = getYProperty().add(NODE_CIRCLE_RADIUS + SELECTION_CIRCLE_THICKNESS);

        initShape();
    }

    private void initShape() {
        circle.setRadius(NODE_CIRCLE_RADIUS);
        circle.setStroke(NODE_CIRCLE_STROKE);
        circle.setStrokeWidth(NODE_CIRCLE_STROKE_WIDTH);

        selectionCircle.setRadius(NODE_CIRCLE_RADIUS + SELECTION_CIRCLE_THICKNESS);
        selectionCircle.setFill(null);
        selectionCircle.setStroke(NODE_CIRCLE_STROKE);
        selectionCircle.setStrokeWidth(SELECTION_CIRCLE_STROKE_WIDTH);
        selectionCircle.setVisible(false);

        label.setText(this.node.getContent());
        label.setStyle("-fx-text-fill: white;");

        pane.getChildren().addAll(circle, selectionCircle, label);
        super.getChildren().add(pane);

        // FIXME: the cursor style is not changed on Mac (Not tested on other platforms)
        this.setCursor(Cursor.MOVE);
    }

    /**
     * {@inheritDoc}
     */
    public void notifySelected() {
        selectionCircle.setVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyUnselected() {
        selectionCircle.setVisible(false);
    }
}
