package io.github.lmhjava.ui.object;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Components that can be rendered on the canvas.
 */
@Data
@EqualsAndHashCode(callSuper = true)

public abstract class CanvasComponent extends Node {
    protected IntegerProperty xProperty;
    protected IntegerProperty yProperty;

    public CanvasComponent(int x, int y) {
        this.xProperty = new SimpleIntegerProperty(x);
        this.yProperty = new SimpleIntegerProperty(y);
        setPosition(x, y);
    }

    public CanvasComponent() {
        this(0, 0);
    }

    public void setPosition(int x, int y) {
        this.xProperty.set(x);
        this.yProperty.set(y);
    }

}
