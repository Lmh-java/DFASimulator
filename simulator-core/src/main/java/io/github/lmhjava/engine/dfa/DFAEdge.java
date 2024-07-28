package io.github.lmhjava.engine.dfa;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class represents a DFA edge connecting two DFA nodes
 *
 */

public class DFAEdge {
    private final DFANode tail;
    private final DFANode head;
    private Set<String> alphabet;
    private boolean isElseEdge;

    public DFAEdge(DFANode tail, DFANode head) {
        this.tail = tail;
        this.head = head;
        this.alphabet = new HashSet<>();
    }

    public DFAEdge(DFANode tail, DFANode head, String alphabet) {
        this.tail = tail;
        this.head = head;
        this.alphabet = new HashSet<>();
        this.alphabet.add(alphabet);
    }

    public boolean isElseEdge() {
        return isElseEdge;
    }

    public void setElseEdge(boolean elseEdge) {
        isElseEdge = elseEdge;
    }

    public DFANode getHead() {
        return head;
    }

    public DFANode getTail() {
        return tail;
    }

    public Set<String> getAlphabet() {
        return new HashSet<>(alphabet);
    }

    public void setAlphabet(Set<String> alphabet) {
        this.alphabet = alphabet;
    }

    public void addAlphabet(String alphabet) {
        this.alphabet.add(alphabet);
    }

    public void addAllAlphabet(Set<String> alphabet) {
        this.alphabet.addAll(alphabet);
    }
}
