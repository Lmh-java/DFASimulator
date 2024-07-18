package io.github.lmhjava.ui.model;

import io.github.lmhjava.engine.edge.DFAEdge;

import java.awt.*;

/**
 * Renderable wrapper of DFA edge.
 */
public class DFAEdgeComponent extends CanvasComponent {

    private DFAEdge edge;

    @Override
    public void render(Graphics2D g2d) {
        g2d.drawLine(100, 100, 200, 200);
        g2d.drawString("ABS", 150, 150);
    }
}
