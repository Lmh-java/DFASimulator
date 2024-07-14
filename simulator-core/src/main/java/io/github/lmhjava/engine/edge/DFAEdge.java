package io.github.lmhjava.engine.edge;

import io.github.lmhjava.engine.node.DFANode;

import java.util.Set;

/**
 * Abstract class represents a DFA edge connecting two DFA nodes
 *
 */

public class DFAEdge {
    private DFANode tail;
    private DFANode head;
    private Set<String> alphabet;

    public DFAEdge(DFANode tail, DFANode head) {
        this.tail = tail;
        this.head = head;
    }

    public DFANode getHead() {
        return head;
    }

    public void setHead(DFANode head) {
        this.head = head;
    }

    public DFANode getTail() {
        return tail;
    }

    public Set<String> getAlphabet() {
        return alphabet;
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

    public void setTail(DFANode tail) {
        this.tail = tail;
    }
}
