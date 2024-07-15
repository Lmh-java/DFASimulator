package io.github.lmhjava.engine.node;

import io.github.lmhjava.engine.edge.DFAEdge;
import io.github.lmhjava.engine.exception.NextNodeUndefException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DFANode {
    // edges store all the edges that starts from the current node
    // e.g. DFAEdges which getTail() == this.
    private List<DFAEdge> edges;
    // cache for quick lookup transitions by inputs
    private final Map<String, DFAEdge> cache;
    // is current node selected
    private boolean onCurrentState;
    // content of the node
    private String content;

    public DFANode() {
        this.edges = new ArrayList<>();
        this.cache = new HashMap<>();
        this.onCurrentState = false;
    }

    public DFANode(String content) {
        this.edges = new ArrayList<>();
        this.cache = new HashMap<>();
        this.onCurrentState = false;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<DFAEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<DFAEdge> edges) {
        this.edges = edges;
        // clear all caches since the edges are updated
        this.cache.clear();
    }

    public boolean isOnCurrentState() {
        return onCurrentState;
    }

    public void setOnCurrentState(boolean onCurrentState) {
        this.onCurrentState = onCurrentState;
    }

    /**
     * Return the next node that DFA will get to, given the input.
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
            // nothing found, throw an exception.
            throw new NextNodeUndefException(this, input);
        }
    }
}
