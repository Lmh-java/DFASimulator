package io.github.lmhjava.engine.exception;

import io.github.lmhjava.engine.dfa.DFANode;

public class NextNodeUndefException extends Exception {
    public NextNodeUndefException(DFANode from, String input) {
        super(String.format("Next node is undefined when transiting from %s with input %s", from, input));
    }
}
