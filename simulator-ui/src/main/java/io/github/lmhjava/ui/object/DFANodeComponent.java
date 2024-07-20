package io.github.lmhjava.ui.object;

import io.github.lmhjava.engine.node.DFANode;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;


/**
 * Renderable wrapper of a DFA node.
 */
public class DFANodeComponent extends CanvasComponent {

    /**
     * Default radius to use when rendering the node
     */
    private static final int NODE_CIRCLE_RADIUS = 40;
    private static final int SELECTION_CIRCLE_THICKNESS = 10;
    private static final int NODE_CIRCLE_STROKE_WIDTH = 5;
    private static final int SELECTION_CIRCLE_STROKE_WIDTH = 2;
    private static final Paint NODE_CIRCLE_STROKE = Color.BLACK;

    private final DFANode node;
    private final Circle circle;
    private final Label label;
    private final Circle selectionCircle;
    private final StackPane pane;

    public DFANodeComponent(int x, int y) {
        super(x, y);
        node = new DFANode("Default");
        pane = new StackPane();

        circle = new Circle();
        circle.setRadius(NODE_CIRCLE_RADIUS);
        circle.setStroke(NODE_CIRCLE_STROKE);
        circle.setStrokeWidth(NODE_CIRCLE_STROKE_WIDTH);

        selectionCircle = new Circle();
        selectionCircle.setRadius(NODE_CIRCLE_RADIUS + SELECTION_CIRCLE_THICKNESS);
        selectionCircle.setFill(null);
        selectionCircle.setStroke(NODE_CIRCLE_STROKE);
        selectionCircle.setStrokeWidth(SELECTION_CIRCLE_STROKE_WIDTH);
        selectionCircle.setVisible(false);

        label = new Label();
        label.setText(this.node.getContent());
        label.setStyle("-fx-text-fill: white;");

        // FIXME: the cursor style is not changed on Mac (Not tested on other platforms)
        this.setCursor(Cursor.MOVE);

        pane.getChildren().addAll(circle, selectionCircle, label);
        super.getChildren().add(pane);
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
