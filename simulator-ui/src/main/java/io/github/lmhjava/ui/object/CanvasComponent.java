package io.github.lmhjava.ui.object;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import lombok.Data;

/**
 * Components that can be rendered on the canvas.
 */
@Data
public abstract class CanvasComponent {
    protected IntegerProperty xProperty;
    protected IntegerProperty yProperty;
    protected DoubleProperty opacityProperty;
    protected Node representation;

    public CanvasComponent(int x, int y) {
        this.xProperty = new SimpleIntegerProperty(x);
        this.yProperty = new SimpleIntegerProperty(y);
        this.opacityProperty = new SimpleDoubleProperty(1D);
        setPosition(x, y);
    }

    public CanvasComponent() {
        this(0, 0);
    }

    public void setPosition(int x, int y) {
        this.xProperty.set(x);
        this.yProperty.set(y);
    }

    public double getOpacity() {
        return opacityProperty.get();
    }

    public void setOpacity(double opacity) {
        this.opacityProperty.set(opacity);
    }
}
