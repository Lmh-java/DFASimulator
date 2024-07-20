package io.github.lmhjava.ui.object;

/**
 * A canvas object that can be selected and reacts to the user selection.
 */
public interface Selectable {
    /**
     * Notify the current object that it is selected, so corresponding UI
     * updates need to be done.
     */
    void notifySelected();

    /**
     * Notify the current object that it is not selected, so corresponding UI
     * updates need to be done.
     */
    void notifyUnselected();
}
