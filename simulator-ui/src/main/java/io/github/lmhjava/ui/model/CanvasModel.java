package io.github.lmhjava.ui.model;

import io.github.lmhjava.ui.object.CanvasComponent;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import lombok.Getter;

/**
 * Data model class for all components being displayed on canvas.
 */
@Getter
public class CanvasModel {
    private final ObservableSet<CanvasComponent> components = FXCollections.observableSet();
    private final ObjectProperty<CanvasComponent> selectedComponent = new SimpleObjectProperty<>();

    private final IntegerProperty contextMenuX = new SimpleIntegerProperty(0);
    private final IntegerProperty contextMenuY = new SimpleIntegerProperty(0);

    private final DoubleProperty scale = new SimpleDoubleProperty(1.0);

    public CanvasComponent getSelectedComponent() {
        return selectedComponent.get();
    }

    public final CanvasComponent getCurrentSelection() {
        return selectedComponent.get();
    }

    public final void setCurrentSelection(CanvasComponent component) {
        // notify the new selection that it is selected and unselect the previous one
        if (selectedComponent.get() != null) {
            selectedComponent.get().notifyUnselected();
        }
        if (component != null) {
            component.notifySelected();
            selectedComponent.set(component);
        }
    }
}
