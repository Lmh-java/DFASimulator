package io.github.lmhjava.engine.dfa;

import io.github.lmhjava.engine.exception.NextNodeUndefException;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A node in a DFA graph.
 */
public class DFANode {
    // edges store all the edges that starts from the current node
    // e.g. DFAEdges which getTail() == this.
    private Set<DFAEdge> edges;
    // one node can only initiate one ELSE edge
    private DFAEdge elseEdge;
    // transition table for quick lookup transitions by inputs
    private final Map<String, DFAEdge> transitionTable;
    // is current node selected
    private boolean onCurrentState;
    // content of the node
    private String content;
    // if this node is accepted
    private boolean isAccepted;

    // a function to call when dfa is transited to current state
    private Runnable onCurrentStateUpdate;

    public DFANode() {
        this.edges = new HashSet<>();
        this.transitionTable = new HashMap<>();
        this.onCurrentState = false;
    }

    public DFANode(String content) {
        this.edges = new HashSet<>();
        this.transitionTable = new HashMap<>();
        this.onCurrentState = false;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<DFAEdge> getEdges() {
        return edges;
    }

    public DFAEdge getElseEdge() {
        return elseEdge;
    }

    protected void setElseEdge(DFAEdge elseEdge) {
        this.elseEdge = elseEdge;
    }

    protected void setEdges(Set<DFAEdge> edges) {
        // clear ELSE edge
        this.elseEdge = null;
        // update edges
        this.edges = edges;
        // clear all caches since the edges are updated
        this.transitionTable.clear();
        // re-build the transition table
        edges.forEach((DFAEdge e) -> {
            if (e.isElseEdge()) {
                this.elseEdge = e;
            } else {
                e.getAlphabets().forEach((String key) -> this.transitionTable.put(key, e));
            }
        });
    }

    public boolean isOnCurrentState() {
        return onCurrentState;
    }

    public void setOnCurrentState(boolean onCurrentState) {
        this.onCurrentState = onCurrentState;
        if (onCurrentState && onCurrentStateUpdate != null) onCurrentStateUpdate.run();
    }

    public void setOnCurrentStateUpdate(Runnable onCurrentStateUpdate) {
        this.onCurrentStateUpdate = onCurrentStateUpdate;
    }

    /**
     * Returns the next node that DFA will get to, given the input.
     *
     * @param input input
     * @return next node
     * @throws NextNodeUndefException if next node is undefined
     */
    protected DFANode getNextNode(String input) throws NextNodeUndefException {
        assert input != null;
        if (transitionTable.containsKey(input)) {
            DFAEdge edge = transitionTable.get(input);
            return edge.getHead();
        } else {
            // check else edge
            if (elseEdge != null) return elseEdge.getHead();
            // nothing found, throw an exception.
            throw new NextNodeUndefException(this, input);
        }
    }

    /**
     * Try to register an edge starting from this node.
     *
     * @param edge the edge being added
     * @return true if successfully added, false otherwise.
     * @implNote
     * 1. if any end of edge contains a non-existing node, this operation will be rejected.
     * 2. if the new edge contains already-registered alphabet in the node (e.g. other edge
     * occupies the alphabet), this operation will be rejected.
     */
    protected boolean registerEdge(DFAEdge edge) {
        assert edge != null;
        // check null preconditions
        if (edge.getTail() != this || edge.getHead() == null || edge.getAlphabets() == null) return false;
        // check if this edge is already registered
        if (edges.contains(edge) || elseEdge == edge) return false;
        // check if this edge contains alphabet key that is already registered
        for (String al : edge.getAlphabets()) {
            if (transitionTable.containsKey(al)) return false;
        }

        // add this edge to this node
        if (edge.isElseEdge()) {
            elseEdge = edge;
        } else {
            edges.add(edge);
            edge.getAlphabets().forEach((String al) -> transitionTable.put(al, edge));
        }
        return true;
    }

    /**
     * delete the edge from this node
     *
     * @implNote the edge should start from this node in order to be deleted. In other words,
     * edge.getTail() == this.
     * @param edge edge being deleted
     * @return whether successfully deleted the edge
     */
    protected boolean removeEdge(DFAEdge edge) {
        assert edge != null;
        if (edge.getTail() != this) return false;
        if (!edges.contains(edge) && elseEdge != edge) return false;
        if (edge.isElseEdge()) {
            elseEdge = null;
            return true;
        }
        for (String al : edge.getAlphabets()) {
            transitionTable.remove(al);
        }
        edges.remove(edge);
        return true;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    /**
     * Bind a new alphabet with the edge
     *
     * @param alphabet alphabet being added
     * @param edge edge bind to the new alphabet
     * @implNote to call this function, caller must ensure the edge is already registered to a node.
     * If not, caller must call addEdge directly. This function is only for adding alphabet for a pre-existing edge.
     *
     * @return successfully added or not
     */
    protected boolean addAlphabet(String alphabet, DFAEdge edge) {
        assert alphabet != null && edge != null;
        // check null preconditions
        if (edge.getTail() != this || edge.getHead() == null || edge.getAlphabets() == null) return false;
        // if edge is not registered to this node before
        if (!edges.contains(edge) || transitionTable.containsKey(alphabet)) return false;

        transitionTable.put(alphabet, edge);
        return true;
    }

    /**
     * Remove an alphabet registered in the node.
     *
     * @implNote if the alphabet is not registered yet, this function will do nothing.
     * @param alphabet alphabet to be removed.
     */
    protected void removeAlphabet(String alphabet) {
        assert alphabet != null;
        transitionTable.remove(alphabet);
    }

    @Override
    public String toString() {
        return "DFANode{" + "Content = '" + content + '\n' +
                "Is Accepted = " + isAccepted + '\n' +
                "On Current State = " + onCurrentState + '\n' +
                "ID = " + super.toString() + "}\n";
    }
}
