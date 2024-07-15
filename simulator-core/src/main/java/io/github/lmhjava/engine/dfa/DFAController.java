package io.github.lmhjava.engine.dfa;

import io.github.lmhjava.engine.edge.DFAEdge;
import io.github.lmhjava.engine.exception.NextNodeUndefException;
import io.github.lmhjava.engine.node.DFANode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DFAController {
    private final List<DFAEdge> edgeList;
    private final List<DFANode> nodeList;
    private DFANode currentNode;
    private DFANode initialNode;
    private Set<String> alphabetSet;

    /**
     * Returns the general alphabet set of this DFA.
     *
     * @return alphabet set of this DFA.
     */
    public Set<String> getAlphabetSet() {
        return new HashSet<>(alphabetSet);
    }

    /**
     * Sets the alphabet set of this DFA.
     *
     * @param alphabetSet new alphabet set
     */
    public void setAlphabetSet(Set<String> alphabetSet) {
        this.alphabetSet = new HashSet<>(alphabetSet);
    }

    /**
     * Returns all nodes in this DFA.
     *
     * @return list of nodes
     */
    public List<DFANode> getNodeList() {
        return new ArrayList<>(nodeList);
    }

    /**
     * Returns all edges in this DFA.
     *
     * @return list of edges
     */
    public List<DFAEdge> getEdgeList() {
        return new ArrayList<>(edgeList);
    }

    /**
     * Returns current node of this DFA.
     *
     * @return reference of current node of DFA.
     */
    public DFANode getCurrentNode() {
        return currentNode;
    }

    /**
     * Activates a new node and deactivates the original.
     * NOTE:
     *  if the new node is not in the node list, this operation will fail.
     *
     * @param currentNode new node to be activated.
     * @return if this operation is successful.
     */
    public boolean setCurrentNode(DFANode currentNode) {
        if (!nodeList.contains(currentNode)) return false;
        if (this.currentNode != null) {
            this.currentNode.setOnCurrentState(false);
        }
        this.currentNode = currentNode;
        this.currentNode.setOnCurrentState(true);
        return true;
    }

    /**
     * Returns a reference to initial node of the DFA.
     * Initial node is the starting node of every input.
     *
     * @return reference of initial node.
     */
    public DFANode getInitialNode() {
        return initialNode;
    }

    /**
     * Sets initial node of DFA.
     *
     * @param initialNode new initial node.
     */
    public void setInitialNode(DFANode initialNode) {
        this.initialNode = initialNode;
    }

    public DFAController() {
        this.edgeList = new ArrayList<>();
        this.nodeList = new ArrayList<>();
        this.alphabetSet = new HashSet<>();
    }

    public DFAController(List<DFAEdge> edgeList, List<DFANode> nodeList,
                         Set<String> alphabetSet, DFANode initialNode) {
        this.edgeList = new ArrayList<>(edgeList);
        this.nodeList = new ArrayList<>(nodeList);
        this.alphabetSet = new HashSet<>(alphabetSet);
        this.initialNode = initialNode;
    }

    /**
     * Shallowly clones the current DFA (Node will be references.)
     *
     * @implNote the new DFA will be at the initial state
     *  which had not experienced any transitions
     * @return shallow copy of the current DFA object.
     */
    public DFAController cloneDFA() {
        return new DFAController(this.edgeList, this.nodeList, this.alphabetSet, this.initialNode);
    }

    /**
     * Add an edge to the current node.
     *
     * @param edge edge being added.
     *             NOTE:
     *             1. if newly-added edge has overlaps with pre-existing edges (same heads),
     *             their alphabets will be merged, which results in only one edge.
     *             2. if newly-added edge contains alphabet which is not in the alphabet set of
     *             the DFA, this operation will be rejected.
     *             3. if any end of edge contains a non-existing node, this operation will be rejected.
     * @return whether successfully added or not.
     */
    public boolean addEdge(DFAEdge edge) {
        assert edge != null;
        // check preconditions
        if (edge.getTail() == null || edge.getAlphabet() == null || edge.getHead() == null) return false;
        // check if the node exists
        if (!nodeList.contains(edge.getTail()) || !nodeList.contains(edge.getHead())) return false;
        // check if the new alphabet is a subset of the general alphabet set
        if (!alphabetSet.containsAll(edge.getAlphabet())) return false;
        // is the newly added edge merged with other pre-existing edges?
        boolean isMergedWithOthers = false;
        for (DFAEdge e : edgeList) {
            if (e.getHead() == edge.getHead() && e.getTail() == edge.getTail()) {
                e.addAllAlphabet(edge.getAlphabet());
                isMergedWithOthers = true;
                break;
            }
        }
        if (!isMergedWithOthers) {
            edgeList.add(edge);
        }
        // update the relevant tail nodes to make sure they are also updated.
        notifyEdgeTail(edge);
        return true;
    }

    /**
     * Update the tail node that has some relationship with the given edge.
     * NOTE: Before calling this method, please make sure {@code this.edgeList} is up-to-date.
     *
     * @param edge relevant edge
     */
    private void notifyEdgeTail(DFAEdge edge) {
        List<DFAEdge> relevantEdges = new ArrayList<>();
        edgeList.forEach((DFAEdge e) -> {
            if (e.getTail() == edge.getTail()) relevantEdges.add(e);
        });
        edge.getTail().setEdges(relevantEdges);
    }

    /**
     * Add a node to the DFA.
     *
     * @param node new node
     * @return successfully added or not.
     */
    public boolean addNode(DFANode node) {
        if (node == null) return false;
        nodeList.add(node);
        return true;
    }

    /**
     * Add a new string to the general alphabet set of the DFA.
     *
     * @param alphabet new string
     * @return whether successfully added
     */
    public boolean addAlphabet(String alphabet) {
        assert alphabet != null;
        if (alphabetSet.contains(alphabet)) {
            return false;
        } else {
            alphabetSet.add(alphabet);
            return true;
        }
    }

    /**
     * Remove the given edge.
     *
     * @param edge edge to be deleted
     */
    public void removeEdge(DFAEdge edge) {
        assert edge != null;
        edgeList.remove(edge);
        // update the corresponding node
        notifyEdgeTail(edge);
    }

    /**
     * Remove the given node from DFA and breaks all relevant edges.
     * NOTE: if the node being deleted is the current node the DFA is on,
     *      the DFA will be reset to the initial node.
     *
     * @param node node to be deleted
     */
    public void removeNode(DFANode node) {
        assert node != null;
        nodeList.remove(node);

        if (node == currentNode) {
            currentNode = initialNode;
            initialNode.setOnCurrentState(true);
        }

        // remove relevant edges
        edgeList.removeIf((DFAEdge e) -> e.getTail() == currentNode || e.getHead() == currentNode);
    }

    /**
     * Forward DFA to the next state according to the input.
     *
     * @param input input that triggers the transition
     * @return next DFA node AFTER transition
     * @throws NextNodeUndefException if the next node is undefined
     */
    public DFANode next(String input) throws NextNodeUndefException {
        assert input != null;
        DFANode nextNode = peek(input);
        currentNode.setOnCurrentState(false);
        nextNode.setOnCurrentState(true);
        currentNode = nextNode;
        return nextNode;
    }

    /**
     * Returns the next node after a transition with given input, WITHOUT actually perform the transition.
     *
     * @param input input that triggers the transition
     * @return next DFA node AFTER transition
     * @throws NextNodeUndefException if the next node is undefined
     */
    public DFANode peek(String input) throws NextNodeUndefException {
        assert input != null;
        return currentNode.getNextNode(input);
    }

}
