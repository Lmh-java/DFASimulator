package io.github.lmhjava.ui.model;

import io.github.lmhjava.engine.dfa.DFAController;
import io.github.lmhjava.ui.object.CanvasComponent;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Data model class for all components being displayed on canvas.
 */
@Getter
@Slf4j
public class CanvasModel {
    private final ObservableSet<CanvasComponent> components;
    private final ObjectProperty<CanvasComponent> selectedComponent;
    private final ObjectProperty<CanvasComponent> highlightedComponent;

    private final IntegerProperty contextMenuX;
    private final IntegerProperty contextMenuY;

    private final DoubleProperty scale;

    private final DFAController dfaController;
    private final ObservableSet<String> dfaAlphabet;

    public CanvasModel() {
        components = FXCollections.observableSet();
        selectedComponent = new SimpleObjectProperty<>();
        highlightedComponent = new SimpleObjectProperty<>();
        contextMenuX = new SimpleIntegerProperty(0);
        contextMenuY = new SimpleIntegerProperty(0);
        scale = new SimpleDoubleProperty(1.0);
        dfaController = new DFAController();
        dfaAlphabet = FXCollections.observableSet();
        // sync this dfa alphabet to the engine
        dfaAlphabet.addListener((SetChangeListener.Change<? extends String> c) -> {
            if (c.wasAdded()) {
                dfaController.registerAlphabet(c.getElementAdded());
            } else if (c.wasRemoved()) {
                dfaController.unregisterAlphabet(c.getElementRemoved());
                // sync all the components with engine models later
                Platform.runLater(() -> components.forEach(CanvasComponent::sync));
            }
        });
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
        }
        selectedComponent.set(component);
    }

    public final void setHighlightedComponent(CanvasComponent component) {
        if (highlightedComponent.get() != null) {
            highlightedComponent.get().deHighlight();
        }
        if (component != null) {
            component.onHighlight();
        }
        selectedComponent.set(component);
    }
}
