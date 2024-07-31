package io.github.lmhjava.engine.observable;

import io.github.lmhjava.engine.dfa.DFANode;

@FunctionalInterface
public interface StateListener {
    void stateChanged(DFANode oldNode, DFANode newNode);
}
