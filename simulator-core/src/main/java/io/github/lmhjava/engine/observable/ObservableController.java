package io.github.lmhjava.engine.observable;

import io.github.lmhjava.engine.dfa.DFANode;

import java.util.HashSet;
import java.util.Set;

/**
 * Subject model for a DFA controller
 */
public class ObservableController {
    protected Set<StateListener> listeners;

    protected ObservableController() {
        listeners = new HashSet<>();
    }

    protected void notifyChange(DFANode oldNode, DFANode newNode) {
        listeners.forEach((listener -> listener.stateChanged(oldNode, newNode)));
    }

    public void addListener(final StateListener listener) {
        listeners.add(listener);
    }
}
