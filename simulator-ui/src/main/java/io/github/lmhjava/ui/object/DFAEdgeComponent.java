package io.github.lmhjava.ui.object;

import io.github.lmhjava.engine.edge.DFAEdge;
import javafx.beans.binding.NumberExpression;
import javafx.beans.property.ObjectProperty;

/**
 * Renderable wrapper of DFA edge.
 */
public class DFAEdgeComponent extends CanvasComponent {

    private DFAEdge edge;

    private ObjectProperty<DFANodeComponent> tailNode;
    private ObjectProperty<DFANodeComponent> headNode;

    private NumberExpression startX;

    @Override
    public void notifySelected() {

    }

    @Override
    public void notifyUnselected() {

    }
}
