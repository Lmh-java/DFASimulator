package io.github.lmhjava.engine.dfa;

import java.util.HashSet;
import java.util.Set;

/**
 * A DFA edge connecting two DFA nodes
 *
 */

public class DFAEdge {
    private final DFANode tail;
    private final DFANode head;
    private final Set<String> alphabets;
    private boolean isElseEdge;
    private boolean isRegistered;

    public DFAEdge(DFANode tail, DFANode head) {
        this.tail = tail;
        this.head = head;
        this.alphabets = new HashSet<>();
    }

    public DFAEdge(DFANode tail, DFANode head, String alphabet) {
        this.tail = tail;
        this.head = head;
        this.alphabets = new HashSet<>();
        this.alphabets.add(alphabet);
    }

    public boolean isElseEdge() {
        return isElseEdge;
    }

    protected boolean isRegistered() {
        return isRegistered;
    }

    protected void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    /**
     * Set this edge to be an else edge and notifies the tail node.
     *
     * @param elseEdge is else edge or not.
     */
    public void setElseEdge(boolean elseEdge) {
        if (this.isRegistered) {
            if (elseEdge) {
                tail.setElseEdge(this);
            } else {
                tail.setElseEdge(null);
            }
        }
        isElseEdge = elseEdge;
    }

    public DFANode getHead() {
        return head;
    }

    public DFANode getTail() {
        return tail;
    }

    public Set<String> getAlphabets() {
        return new HashSet<>(alphabets);
    }

    /**
     * Register an alphabet for this edge and update the tail node.
     *
     * @implNote the caller must ensure the new alphabet is registered in the associated {@code DFAController}
     * to avoid unexpected behaviors.
     * @param alphabet new alphabet
     */
    public void registerAlphabet(String alphabet) {
        assert alphabet != null;
        if (alphabets.contains(alphabet)) return;
        if (this.isRegistered) {
            this.tail.addAlphabet(alphabet, this);
        }
        this.alphabets.add(alphabet);
    }

    /**
     * Unregister an alphabet from this edge and update the tail node.
     *
     * @implNote if alphabet is not registered before this function is called,
     * this function fails silently.
     * @param alphabet alphabet to unregister
     */
    public void unregisterAlphabet(String alphabet) {
        assert alphabet != null;
        if (!alphabets.contains(alphabet)) return;
        if (this.isRegistered) {
            this.tail.removeAlphabet(alphabet);
        }
        this.alphabets.remove(alphabet);
    }

    /**
     * Add all alphabet in a batch
     *
     * @param alphabets a set of alphabets
     */
    public void registerAllAlphabet(Set<String> alphabets) {
        alphabets.forEach(this::registerAlphabet);
    }

    /**
     * Add all alphabet in a batch
     *
     * @param alphabets a set of alphabets
     */
    public void registerAllAlphabet(String... alphabets) {
        for (String al: alphabets) {
            registerAlphabet(al);
        }
    }

    @Override
    public String toString() {
        return "DFAEdge{\nTail Node = " + tail + "\n" +
                "Head Node = " + head + "\n" +
                "Alphabets = " + alphabets + "\n" +
                "Is Else Edge = " + isElseEdge + "\n" +
                "Is Registered = " + isRegistered + "\n" +
                "ID = " + super.toString() + "}\n";
    }
}
