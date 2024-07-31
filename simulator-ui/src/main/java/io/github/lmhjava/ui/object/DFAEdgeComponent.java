package io.github.lmhjava.ui.object;

import io.github.lmhjava.engine.dfa.DFAEdge;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.transform.Transform;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.StringJoiner;

/**
 * Renderable wrapper of DFA edge.
 * TODO: refactor: an arrow should be an individual component
 */
@Slf4j
public class DFAEdgeComponent extends CanvasComponent {

    private static final Paint ARROW_COLOR = Color.RED;
    private static final int ARROW_STROKE_THICKNESS = 2;
    private static final int SPACE_WIDTH = 10;
    private static final int ARROW_HEAD_LENGTH = 20;
    private static final int ARROW_HEAD_ANGLE = 30;
    private static final double LABEL_ARROW_GAP = 15;
    private static final double ARROW_HEAD_DELTA_Y = Math.sin(Math.toRadians(ARROW_HEAD_ANGLE)) * ARROW_HEAD_LENGTH;
    private static final double SELF_LOOP_ARROW_HEAD_LENGTH = ARROW_HEAD_LENGTH / 2d;
    private static final double SELF_LOOP_ARROW_HEAD_ANGLE = ARROW_HEAD_ANGLE / 2d;

    private DFAEdge edge;
    private final ObjectProperty<DFANodeComponent> tailNodeObj;
    private final ObjectProperty<DFANodeComponent> headNodeObj;

    private boolean isSettled;

    private final StackPane pane;

    private final Path arrow;
    private final QuadCurveTo baseLine;
    private final MoveTo originPoint;
    private final LineTo leftArrowHead;
    private final LineTo rightArrowHead;
    private final MoveTo tipPoint;
    private final Label alphabetLabel;

    @Getter
    private final ObservableSet<String> alphabets;
    @Getter
    private final BooleanProperty isElseProperty;

    // These two following properties will not be used until edge is settled
    private DoubleBinding elevationAngleProperty;
    private IntegerBinding lengthProperty;

    public DFAEdgeComponent(DFANodeComponent tailNode) {
        this.tailNodeObj = new SimpleObjectProperty<>(tailNode);
        this.pane = new StackPane();
        this.headNodeObj = new SimpleObjectProperty<>(null);
        this.isSettled = false;
        this.arrow = new Path();
        this.baseLine = new QuadCurveTo();
        this.leftArrowHead = new LineTo();
        this.rightArrowHead = new LineTo();
        this.originPoint = new MoveTo(0, 0);
        this.tipPoint = new MoveTo(0, 0);
        this.alphabetLabel = new Label();
        this.alphabets = FXCollections.observableSet();
        this.elevationAngleProperty = null;
        this.lengthProperty = null;
        this.isElseProperty = new SimpleBooleanProperty(false);
    }

    /**
     * Initialize the properties listeners to sync information from the core DFA engine.
     */
    private void initPropertyListeners() {
        this.alphabets.addListener((SetChangeListener.Change<? extends String> c) -> {
            if (c.wasAdded()) {
                edge.registerAlphabet(c.getElementAdded());
            } else if (c.wasRemoved()) {
                edge.unregisterAlphabet(c.getElementRemoved());
            }
        });
        this.alphabetLabel.textProperty().bind(new StringBinding() {
            {
                this.bind(alphabets, isElseProperty);
            }

            @Override
            protected String computeValue() {
                if (isElseProperty.get()) return "ELSE";
                final StringJoiner sj = new StringJoiner(", ", "{", "}");
                alphabets.forEach(sj::add);
                return sj.toString();
            }
        });
        this.isElseProperty.addListener((observable, oldValue, newValue) -> {
            edge.setElseEdge(newValue);
        });
    }

    /**
     * Initialize cursor to match the direction of the arrow.
     *
     * @implNote For better user experience, the cursor show display resize prompt in the direction
     * perpendicular to the direction of the arrow.
     */
    private void initCursor() {
        assert !isSelfLoop();
        // show cursors depending on whether the arrow is vertical or horizontal.
        this.cursorProperty().bind(new ObjectBinding<>() {
            {
                this.bind(elevationAngleProperty);
            }

            @Override
            protected Cursor computeValue() {
                return isVertical() ? Cursor.H_RESIZE : Cursor.V_RESIZE;
            }
        });
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
        this.getYProperty().bind(tailNodeObj.get().getCenterYProperty().subtract(ARROW_HEAD_DELTA_Y));

        // create a pseudo-node to help with locating the end node
        headNodeObj.set(null);

        // we don't bind properties here, since the properties can be calculated dynamically by mouse moving listeners
        baseLine.yProperty().set(0);
        final EventHandler<MouseEvent> syncMouseHandler = (event) -> {
            final int headX = (int) event.getX(), headY = (int) event.getY();
            final int tailX = tailNodeObj.get().getCenterXProperty().intValue(), tailY = tailNodeObj.get().getCenterYProperty().intValue();
            // rotate the arrow to make sure it aims to the mouse
            this.getTransforms().clear();
            final Transform rotate = Transform.rotate(Math.toDegrees(Math.atan2(headY - tailY, headX - tailX)), 0, ARROW_HEAD_DELTA_Y);
            this.getTransforms().add(rotate);
            // length of the arrow is the distance between two nodes
            final double length = Math.sqrt(Math.pow((headX - tailX), 2) + Math.pow((headY - tailY), 2)) - SPACE_WIDTH;
            baseLine.setX(length);
            baseLine.setControlX(length / 2);
            // calculate two noses of the arrow
            tipPoint.xProperty().set(length);
            final double arrowHeadX = length - Math.cos(Math.toRadians(ARROW_HEAD_ANGLE)) * ARROW_HEAD_LENGTH;
            leftArrowHead.xProperty().set(arrowHeadX);
            rightArrowHead.xProperty().set(arrowHeadX);
            leftArrowHead.yProperty().set(-ARROW_HEAD_DELTA_Y);
            rightArrowHead.yProperty().set(ARROW_HEAD_DELTA_Y);
            event.consume();
        };
        canvasPane.setOnMouseMoved(syncMouseHandler);
        arrow.getElements().addAll(originPoint, baseLine, leftArrowHead, tipPoint, rightArrowHead);

        pane.getChildren().addAll(arrow, alphabetLabel);
        getChildren().add(pane);
    }

    public void settle(DFANodeComponent headNode, Pane canvasPane) {
        assert headNode != null && canvasPane != null;
        isSettled = true;
        canvasPane.setOnMouseMoved(null);
        headNodeObj.set(headNode);
        // self-loop edge
        if (isSelfLoop()) {
            baseLine.setControlX(DFANodeComponent.NODE_CIRCLE_RADIUS / 2d);
            baseLine.setControlY(DFANodeComponent.NODE_CIRCLE_RADIUS * 2);
            baseLine.setX(DFANodeComponent.NODE_CIRCLE_RADIUS);
            baseLine.setY(0);
            tipPoint.setX(DFANodeComponent.NODE_CIRCLE_RADIUS);
            tipPoint.setY(0);
            leftArrowHead.setX(DFANodeComponent.NODE_CIRCLE_RADIUS - ARROW_HEAD_DELTA_Y);
            leftArrowHead.setY(SELF_LOOP_ARROW_HEAD_LENGTH * Math.cos(Math.toRadians(SELF_LOOP_ARROW_HEAD_ANGLE)));
            rightArrowHead.setX(DFANodeComponent.NODE_CIRCLE_RADIUS + ARROW_HEAD_DELTA_Y / 2);
            rightArrowHead.setY(SELF_LOOP_ARROW_HEAD_LENGTH * Math.cos(Math.toRadians(SELF_LOOP_ARROW_HEAD_ANGLE)));
            this.getTransforms().clear();
            this.getTransforms().add(Transform.rotate(-180, 0, 0));
            this.getTransforms().add(Transform.translate(-DFANodeComponent.NODE_CIRCLE_RADIUS, DFANodeComponent.NODE_CIRCLE_RADIUS - DFANodeComponent.SELECTION_CIRCLE_THICKNESS));
            // mock elevation angle property for controlling the alphabet label
            this.elevationAngleProperty = new DoubleBinding() {
                @Override
                protected double computeValue() {
                    return -180;
                }
            };
        } else {
            // unlike syncMouseHandler, we need to dynamic binding to calculate rotated degrees and length
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
            this.getTransforms().add(Transform.rotate(this.elevationAngleProperty.get(), 0, ARROW_HEAD_DELTA_Y));
            this.elevationAngleProperty.addListener((ob, oldValue, newValue) -> {
                this.getTransforms().clear();
                this.getTransforms().add(Transform.rotate(this.elevationAngleProperty.get(), 0, ARROW_HEAD_DELTA_Y));
            });

            // bind length property
            baseLine.xProperty().bind(this.lengthProperty);
            baseLine.controlXProperty().bind(this.lengthProperty.divide(2));
            // make two noses of the arrow
            handleArrowNoses();

            // translate Y and X of the region to complement the change in concurvity of the arrow
            this.translateYProperty().bind(Bindings.createDoubleBinding(() -> baseLine.controlYProperty().get() < 0
                            ? baseLine.getControlY() / 2 * Math.sin(Math.toRadians(90 - elevationAngleProperty.get()))
                            : 0
                    , baseLine.controlYProperty(), elevationAngleProperty));
            this.translateXProperty().bind(Bindings.createDoubleBinding(() ->
                            baseLine.controlYProperty().get() < 0
                                    ? -baseLine.getControlY() / 2 * Math.cos(Math.toRadians(90 - elevationAngleProperty.get()))
                                    : 0
                    , baseLine.controlYProperty(), elevationAngleProperty));
            initCursor();
        }

        // move two nodes on two ends to the front layer
        headNodeObj.get().toFront();
        tailNodeObj.get().toFront();

        handleLabelPosition();

        // initiate an edge model
        this.edge = new DFAEdge(tailNodeObj.get().getNode(), headNodeObj.get().getNode());
        initPropertyListeners();
    }

    /**
     * Calculates (dynamically) the arrow nose positions for non-self-loop edges
     *
     * @implNote this function only handles non-self-loop edges
     */
    private void handleArrowNoses() {
        assert !isSelfLoop();
        // the angle between the line connecting the vertex of the curve with one end of the curve and the original horizontal line before bending.
        tipPoint.xProperty().bind(lengthProperty);
        final DoubleBinding deltaAngle = Bindings.createDoubleBinding(() -> -Math.atan2(baseLine.getControlY(), (double) lengthProperty.get() / 2), baseLine.controlYProperty(), lengthProperty);
        final DoubleBinding originalX = this.lengthProperty.subtract(Math.cos(Math.toRadians(ARROW_HEAD_ANGLE)) * ARROW_HEAD_LENGTH);
        leftArrowHead.xProperty().bind(Bindings.createDoubleBinding(() -> (originalX.get() - lengthProperty.get()) * Math.cos(deltaAngle.get()) + ARROW_HEAD_DELTA_Y * Math.sin(deltaAngle.get()) + lengthProperty.get(), deltaAngle, originalX, lengthProperty));
        leftArrowHead.yProperty().bind(Bindings.createDoubleBinding(() -> (originalX.get() - lengthProperty.get()) * Math.sin(deltaAngle.get()) - ARROW_HEAD_DELTA_Y * Math.cos(deltaAngle.get()), deltaAngle, lengthProperty));
        rightArrowHead.xProperty().bind(Bindings.createDoubleBinding(() -> (originalX.get() - lengthProperty.get()) * Math.cos(deltaAngle.get()) - ARROW_HEAD_DELTA_Y * Math.sin(deltaAngle.get()) + lengthProperty.get(), deltaAngle, originalX, lengthProperty));
        rightArrowHead.yProperty().bind(Bindings.createDoubleBinding(() -> (originalX.get() - lengthProperty.get()) * Math.sin(deltaAngle.get()) + ARROW_HEAD_DELTA_Y * Math.cos(deltaAngle.get()), deltaAngle, lengthProperty));
    }

    /**
     * Calculates (dynamically) the edge label position according to the position of the arrow.
     */
    private void handleLabelPosition() {
        assert isSettled;
        // no matter which direction the arrow is, the label is always up and above the arrow.
        // if the arrow goes from left to right (-90 < angle < 90)
        // if the arrow goes from right to left (angle >= 90 || angle =< -90)
        alphabetLabel.translateYProperty().bind(Bindings.createDoubleBinding(
                () -> (isLeftToRight()
                        ? -LABEL_ARROW_GAP
                        : LABEL_ARROW_GAP + (isSelfLoop() ? LABEL_ARROW_GAP : 0))
                        + (isSelfLoop() ? 0 : this.baseLine.getControlY() / 4)
                , elevationAngleProperty, this.baseLine.controlYProperty()));
        // rotate the label to make sure the label is straight up.
        alphabetLabel.rotateProperty().bind(Bindings.createDoubleBinding(
                () -> isLeftToRight()
                        ? 0d
                        : 180d, elevationAngleProperty));
    }

    public boolean isSelfLoop() {
        return headNodeObj.get() == tailNodeObj.get();
    }

    public boolean isVertical() {
        return (45 < this.elevationAngleProperty.get() && this.elevationAngleProperty.get() < 135)
                || (this.elevationAngleProperty.get() > -135 && this.elevationAngleProperty.get() < -45);
    }

    public boolean isLeftToRight() {
        return -90 < elevationAngleProperty.get() && elevationAngleProperty.get() < 90;
    }

    public boolean isUpToDown() {
        return 45 < this.elevationAngleProperty.get() && this.elevationAngleProperty.get() < 135;
    }

    @Override
    public void notifySelected() {
        arrow.setEffect(new Glow(1.5));
    }

    @Override
    public void notifyUnselected() {
        arrow.setEffect(null);
    }

    /**
     * Sets the value of controlY coordinate of the baseline of the arrow
     *
     * @param newControlVal new control value
     */
    public void setArrowControl(double newControlVal) {
        this.baseLine.setControlY(newControlVal);
    }

    /**
     * Set context menu of this edge
     *
     * @param menuHandler function to trigger when menu is requested
     */
    public void setContextMenu(EventHandler<ContextMenuEvent> menuHandler) {
        arrow.setOnContextMenuRequested(menuHandler);
        alphabetLabel.setOnContextMenuRequested(menuHandler);
    }

    public DFANodeComponent getTailNode() {
        return tailNodeObj.get();
    }

    public DFANodeComponent getHeadNode() {
        return headNodeObj.get();
    }

    @Override
    public void sync() {
        alphabets.clear();
        alphabets.addAll(edge.getAlphabets());
        isElseProperty.set(edge.isElseEdge());
    }

    @Override
    public void onHighlight() {

    }

    @Override
    public void deHighlight() {

    }
}
