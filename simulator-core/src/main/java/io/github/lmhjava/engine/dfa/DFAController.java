package io.github.lmhjava.engine.dfa;

import io.github.lmhjava.engine.edge.DFAEdge;
import io.github.lmhjava.engine.exception.NextNodeUndefException;
import io.github.lmhjava.engine.node.DFANode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DFAController {
    private final Set<DFAEdge> edgeSet;
    private final Set<DFANode> nodeSet;
    private DFANode currentNode;
    private DFANode initialNode;
    private Set<String> alphabetSet;

    public DFAController() {
        this.edgeSet = new HashSet<>();
        this.nodeSet = new HashSet<>();
        this.alphabetSet = new HashSet<>();
    }

    public DFAController(Set<DFAEdge> edgeSet, Set<DFANode> nodeSet,
                         Set<String> alphabetSet, DFANode initialNode) {
        this.edgeSet = new HashSet<>(edgeSet);
        this.nodeSet = new HashSet<>(nodeSet);
        this.alphabetSet = new HashSet<>(alphabetSet);
        this.initialNode = initialNode;
    }

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
    public List<DFANode> getNodeSet() {
        return new ArrayList<>(nodeSet);
    }

    /**
     * Returns all edges in this DFA.
     *
     * @return list of edges
     */
    public List<DFAEdge> getEdgeSet() {
        return new ArrayList<>(edgeSet);
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
        if (currentNode == null || !nodeSet.contains(currentNode)) return false;
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
        assert nodeSet.contains(initialNode);
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
        return new DFAController(this.edgeSet, this.nodeSet, this.alphabetSet, this.initialNode);
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
        // TODO: complete checks for duplicate transitions (duplicate trigger alphabet)
        assert edge != null;
        // check preconditions
        if (edge.getTail() == null || edge.getAlphabet() == null || edge.getHead() == null) return false;
        // check if the node exists
        if (!nodeSet.contains(edge.getTail()) || !nodeSet.contains(edge.getHead())) return false;
        // check if the new alphabet is a subset of the general alphabet set
        if (!alphabetSet.containsAll(edge.getAlphabet())) return false;
        // is the newly added edge merged with other pre-existing edges?
        boolean isMergedWithOthers = false;
        for (DFAEdge e : edgeSet) {
            if (e.getHead() == edge.getHead() && e.getTail() == edge.getTail()) {
                e.addAllAlphabet(edge.getAlphabet());
                isMergedWithOthers = true;
                break;
            }
        }
        if (!isMergedWithOthers) {
            edgeSet.add(edge);
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
        edgeSet.forEach((DFAEdge e) -> {
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
        nodeSet.add(node);
        return true;
    }

    /**
     * Add a new string to the general alphabet set of the DFA.
     *
     * @param alphabet new string
     * @return whether successfully added
     */
    public boolean registerAlphabet(String alphabet) {
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
        edgeSet.remove(edge);
        // update the corresponding node
        notifyEdgeTail(edge);
    }

    /**
     * Remove the given node from DFA and breaks all relevant edges.
     * NOTE: If the node being deleted is the current node the DFA is on,
     *      the DFA will be reset to the initial node.
     *      If the node being deleted is the initial node, the initial node
     *      will be reset to null. (This helps GC to collect unused pointers)
     *
     * @param node node to be deleted
     */
    public void removeNode(DFANode node) {
        assert node != null;
        nodeSet.remove(node);

        if (node == currentNode) {
            currentNode = initialNode;
            initialNode.setOnCurrentState(true);
        }

        if (node == initialNode) {
            initialNode = null;
        }

        // remove relevant edges
        edgeSet.removeIf((DFAEdge e) -> e.getTail() == node || e.getHead() == node);
    }

    /**
     * Forward DFA to the next state according to the input.
     *
     * @param input input that triggers the transition
     * @implNote If current node is {@code null}, DFA will look for the initial node.
     *          If initial node is also {@code null}, throw {@code NextNodeUndefException}.
     * @return next DFA node AFTER transition
     * @throws NextNodeUndefException if the next node is undefined
     */
    public DFANode next(String input) throws NextNodeUndefException {
        assert input != null;
        if (currentNode == null) {
            if (initialNode == null) {
                throw new NextNodeUndefException(null, input);
            } else {
                currentNode = initialNode;
            }
        }
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
     * @implNote If current node is {@code null}, DFA will look for the initial node.
     *          If initial node is also {@code null}, throw {@code NextNodeUndefException}.
     *          PS: Unlike {@link  DFAController#next(String)}, peek will not modify any properties of DFA.
     * @return next DFA node AFTER transition
     * @throws NextNodeUndefException if the next node is undefined
     */
    public DFANode peek(String input) throws NextNodeUndefException {
        assert input != null;
        if (currentNode == null) {
            if (initialNode == null) {
                throw new NextNodeUndefException(null, input);
            } else {
                return initialNode.getNextNode(input);
            }
        }
        return currentNode.getNextNode(input);
    }

}
