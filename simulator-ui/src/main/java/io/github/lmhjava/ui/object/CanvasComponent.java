package io.github.lmhjava.ui.object;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.Region;
import lombok.EqualsAndHashCode;
import lombok.Getter;


/**
 * Components that can be rendered on the canvas.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public abstract class CanvasComponent extends Region implements Selectable {
    protected final IntegerProperty xProperty;
    protected final IntegerProperty yProperty;

    public CanvasComponent(int x, int y) {
        this.xProperty = new SimpleIntegerProperty(x);
        this.yProperty = new SimpleIntegerProperty(y);
        setPosition(x, y);
        super.layoutXProperty().bind(xProperty);
        super.layoutYProperty().bind(yProperty);
    }

    public CanvasComponent() {
        this(0, 0);
    }

    public void setPosition(int x, int y) {
        this.xProperty.set(x);
        this.yProperty.set(y);
    }

    /**
     * {@inheritDoc}
     */
    public abstract void notifySelected();

    /**
     * {@inheritDoc}
     */
    public abstract void notifyUnselected();
}
