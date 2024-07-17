package io.github.lmhjava.engine.node;

import io.github.lmhjava.engine.edge.DFAEdge;
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
    // local alphabet of the node, meaning which alphabets are allowed in this node.
    private final Set<String> alphabet;
    // cache for quick lookup transitions by inputs
    private final Map<String, DFAEdge> cache;
    // is current node selected
    private boolean onCurrentState;
    // content of the node
    private String content;

    public DFANode() {
        this.edges = new HashSet<>();
        this.cache = new HashMap<>();
        this.alphabet = new HashSet<>();
        this.onCurrentState = false;
    }

    public DFANode(String content) {
        this.edges = new HashSet<>();
        this.cache = new HashMap<>();
        this.alphabet = new HashSet<>();
        this.onCurrentState = false;
        this.content = content;
    }

    public Set<String> getAlphabet() {
        return alphabet;
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

    public void setElseEdge(DFAEdge elseEdge) {
        this.elseEdge = elseEdge;
    }

    public void setEdges(Set<DFAEdge> edges) {
        // clear ELSE edge
        this.elseEdge = null;
        // update edges
        this.edges = edges;
        // clear all caches since the edges are updated
        this.cache.clear();
        // update the alphabet set of the node
        this.alphabet.clear();
        // re-compute the alphabet subset of this node
        edges.forEach((DFAEdge e) -> {
            if (e.isElseEdge()) {
                this.elseEdge = e;
            } else {
                this.alphabet.addAll(e.getAlphabet());
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
    public DFANode getNextNode(String input) throws NextNodeUndefException {
        assert input != null;
        if (cache.containsKey(input)) {
            DFAEdge edge = cache.get(input);
            return edge.getHead();
        } else {
            for (DFAEdge edge : edges) {
                if (edge.getAlphabet().contains(input)) {
                    // update the cache
                    cache.put(input, edge);
                    return edge.getHead();
                }
            }
            // check else edge
            if (elseEdge != null) return elseEdge.getHead();
            // nothing found, throw an exception.
            throw new NextNodeUndefException(this, input);
        }
    }
}
