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
                e.getAlphabet().forEach((String key) -> this.transitionTable.put(key, e));
            }
        });
    }

    public boolean isOnCurrentState() {
        return onCurrentState;
    }

    public void setOnCurrentState(boolean onCurrentState) {
        this.onCurrentState = onCurrentState;
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
        if (edge.getTail() != this || edge.getHead() == null || edge.getAlphabet() == null) return false;
        // check if alphabet set of the edge is empty
        if (edge.getAlphabet().isEmpty() && !edge.isElseEdge()) return false;
        // check if this edge is already registered
        if (edges.contains(edge) || elseEdge == edge) return false;
        // check if this edge contains alphabet key that is already registered
        for (String al : edge.getAlphabet()) {
            if (transitionTable.containsKey(al)) return false;
        }

        // add this edge to this node
        if (edge.isElseEdge()) {
            elseEdge = edge;
        } else {
            edges.add(edge);
            edge.getAlphabet().forEach((String al) -> transitionTable.put(al, edge));
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
        for (String al : edge.getAlphabet()) {
            transitionTable.remove(al);
        }
        edges.remove(edge);
        return true;
    }
}
