package io.github.lmhjava.ui.object;

import io.github.lmhjava.engine.edge.DFAEdge;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.transform.Transform;
import lombok.extern.slf4j.Slf4j;

/**
 * Renderable wrapper of DFA edge.
 */
@Slf4j
public class DFAEdgeComponent extends CanvasComponent {

    private static final Paint ARROW_COLOR = Color.RED;
    private static final int ARROW_STROKE_THICKNESS = 2;
    private static final int SPACE_WIDTH = 10;
    private static final int ARROW_HEAD_LENGTH = 20;
    private static final int ARROW_HEAD_ANGLE = 30;

    private DFAEdge edge;
    private final ObjectProperty<DFANodeComponent> tailNodeObj;
    private final ObjectProperty<DFANodeComponent> headNodeObj;

    private boolean isSettled;

    private final StackPane pane;

    private final Path arrow;
    private final LineTo baseLine;
    private final MoveTo originPoint;
    private final LineTo leftArrowHead;
    private final LineTo rightArrowHead;
    private final MoveTo tipPoint;

    // These two following properties will not be used until edge is settled
    private DoubleBinding elevationAngleProperty;
    private IntegerBinding lengthProperty;

    public DFAEdgeComponent(DFANodeComponent tailNode) {
        this.tailNodeObj = new SimpleObjectProperty<>(tailNode);
        this.pane = new StackPane();
        // FIXME: delete the following line of test code
        this.pane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.4);");
        this.headNodeObj = new SimpleObjectProperty<>(null);
        this.isSettled = false;
        this.arrow = new Path();
        this.baseLine = new LineTo();
        this.leftArrowHead = new LineTo();
        this.rightArrowHead = new LineTo();
        this.originPoint = new MoveTo(0, 0);
        this.tipPoint = new MoveTo(0, 0);
        this.elevationAngleProperty = null;
        this.lengthProperty = null;
    }

    /**
     * Initialize the shape of an edge component
     *
     * @implNote at this moment, {@code isSettle} is {@code false}.
     */
    public void initShape(Pane canvasPane) {
        // construct an arrow based on head and tail node
        arrow.setStroke(ARROW_COLOR);
        arrow.setStrokeWidth(ARROW_STROKE_THICKNESS);
        this.getXProperty().bind(tailNodeObj.get().getCenterXProperty());
        this.getYProperty().bind(tailNodeObj.get().getCenterYProperty());

        // create a pseudo-node to help with locating the end node
        headNodeObj.set(null);

        // we don't bind properties here, since the properties can be calculated dynamically by mouse moving listeners
        baseLine.yProperty().set(0);
        final EventHandler<MouseEvent> syncMouseHandler = (event) -> {
            final int headX = (int) event.getX(), headY = (int) event.getY();
            final int tailX = tailNodeObj.get().getCenterXProperty().intValue(), tailY = tailNodeObj.get().getCenterYProperty().intValue();
            // rotate the arrow to make sure it aims to the mouse
            this.getTransforms().clear();
            final Transform rotate = Transform.rotate(Math.toDegrees(Math.atan2(headY - tailY, headX - tailX)), 0, 0);
            this.getTransforms().add(rotate);
            // length of the arrow is the distance between two nodes
            final double length = Math.sqrt(Math.pow((headX - tailX), 2) + Math.pow((headY - tailY), 2)) - SPACE_WIDTH;
            baseLine.xProperty().set(length);
            // calculate two noses of the arrow
            tipPoint.xProperty().set(length);
            final double arrowHeadX = length - Math.cos(Math.toRadians(ARROW_HEAD_ANGLE)) * ARROW_HEAD_LENGTH;
            final double arrowHeadDeltaY = Math.sin(Math.toRadians(ARROW_HEAD_ANGLE)) * ARROW_HEAD_LENGTH;
            leftArrowHead.xProperty().set(arrowHeadX);
            rightArrowHead.xProperty().set(arrowHeadX);
            leftArrowHead.yProperty().set(-arrowHeadDeltaY);
            rightArrowHead.yProperty().set(arrowHeadDeltaY);
            event.consume();
        };
        canvasPane.setOnMouseMoved(syncMouseHandler);
        arrow.getElements().addAll(originPoint, baseLine, leftArrowHead, tipPoint, rightArrowHead);

        pane.getChildren().addAll(arrow);
        getChildren().add(pane);
    }

    public void settle(DFANodeComponent headNode, Pane canvasPane) {
        if (headNode != null) {
            canvasPane.setOnMouseMoved(null);
            headNodeObj.set(headNode);
            // unlike syncMouseHandler, here, we need to dynamic binding to calculate rotated degrees and length
            this.elevationAngleProperty = Bindings.createDoubleBinding(() -> {
                final int headX = headNodeObj.get().getXProperty().get(), headY = headNodeObj.get().getYProperty().get();
                final int tailX = tailNodeObj.get().getXProperty().get(), tailY = tailNodeObj.get().getYProperty().get();
                return Math.toDegrees(Math.atan2(headY - tailY, headX - tailX));
            }, headNodeObj.get().getXProperty(), headNodeObj.get().getYProperty(), tailNodeObj.get().getXProperty(), tailNodeObj.get().getYProperty());

            this.lengthProperty = Bindings.createIntegerBinding(() -> {
                final int headX = headNodeObj.get().getXProperty().get(), headY = headNodeObj.get().getYProperty().get();
                final int tailX = tailNodeObj.get().getXProperty().get(), tailY = tailNodeObj.get().getYProperty().get();
                return (int) Math.sqrt(Math.pow(headX - tailX, 2) + Math.pow(headY - tailY, 2)) - DFANodeComponent.NODE_CIRCLE_RADIUS - DFANodeComponent.SELECTION_CIRCLE_THICKNESS;
            }, headNodeObj.get().getXProperty(), headNodeObj.get().getYProperty(), tailNodeObj.get().getXProperty(), tailNodeObj.get().getYProperty());

            // bind angle property
            this.getTransforms().clear();
            this.getTransforms().add(Transform.rotate(this.elevationAngleProperty.get(), 0, 0));
            this.elevationAngleProperty.addListener((ob, oldValue, newValue) -> {
                this.getTransforms().clear();
                this.getTransforms().add(Transform.rotate(this.elevationAngleProperty.get(), 0, 0));
            });

            // bind length property
            baseLine.xProperty().bind(this.lengthProperty);
            tipPoint.xProperty().bind(this.lengthProperty);
            // make two noses of the arrow
            leftArrowHead.xProperty().bind(this.lengthProperty.subtract(Math.cos(Math.toRadians(ARROW_HEAD_ANGLE)) * ARROW_HEAD_LENGTH));
            rightArrowHead.xProperty().bind(this.lengthProperty.subtract(Math.cos(Math.toRadians(ARROW_HEAD_ANGLE)) * ARROW_HEAD_LENGTH));
            final double arrowHeadDeltaY = Math.sin(Math.toRadians(ARROW_HEAD_ANGLE)) * ARROW_HEAD_LENGTH;
            leftArrowHead.yProperty().set(-arrowHeadDeltaY);
            rightArrowHead.yProperty().set(arrowHeadDeltaY);
            // move two nodes on two ends to the front layer
            headNodeObj.get().toFront();
            tailNodeObj.get().toFront();
        }
        isSettled = true;
    }

    @Override
    public void notifySelected() {

    }

    @Override
    public void notifyUnselected() {

    }
}
