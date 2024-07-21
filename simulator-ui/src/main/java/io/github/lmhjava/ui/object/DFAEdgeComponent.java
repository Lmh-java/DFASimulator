package io.github.lmhjava.ui.object;

import io.github.lmhjava.engine.edge.DFAEdge;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.Path;

/**
 * Renderable wrapper of DFA edge.
 */
public class DFAEdgeComponent extends CanvasComponent {

    private DFAEdge edge;

    private final ObjectProperty<DFANodeComponent> tailNode;
    private final ObjectProperty<DFANodeComponent> headNode;

    private boolean isSettled;
    private final IntegerProperty startX;
    private final IntegerProperty startY;
    private final IntegerProperty endX;
    private final IntegerProperty endY;

    private Path arrowPath;

    public DFAEdgeComponent(DFANodeComponent tailNode) {
        this.tailNode = new SimpleObjectProperty<>(tailNode);
        this.headNode = new SimpleObjectProperty<>(null);
        this.isSettled = false;
        this.startX = new SimpleIntegerProperty();
        this.startY = new SimpleIntegerProperty();
        this.endX = new SimpleIntegerProperty();
        this.endY = new SimpleIntegerProperty();
    }

    public void initShape() {

    }

    public void settle(DFANodeComponent headNode) {
        this.headNode.set(headNode);
        isSettled = true;
    }

    @Override
    public void notifySelected() {

    }

    @Override
    public void notifyUnselected() {

    }
}
