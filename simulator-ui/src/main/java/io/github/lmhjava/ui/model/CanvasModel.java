package io.github.lmhjava.ui.model;

import io.github.lmhjava.ui.object.CanvasComponent;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
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

    public CanvasComponent getSelectedComponent() {
        return selectedComponent.get();
    }

    public final CanvasComponent getCurrentSelection() {
        return selectedComponent.get();
    }

    public final void setCurrentSelection(CanvasComponent component) {
        selectedComponent.set(component);
    }
}
