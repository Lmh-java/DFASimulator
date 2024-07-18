package io.github.lmhjava.ui.model;

import java.awt.*;

/**
 * Components that can be rendered on the canvas.
 */
public abstract class CanvasComponent {
    protected int x;
    protected int y;
    protected double opacity;

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public abstract void render(Graphics2D g2d);
}
