package io.github.lmhjava.engine.dfa;

import io.github.lmhjava.engine.exception.NextNodeUndefException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main controller of a DFA.
 * Each DFA has a corresponding controller.
 */
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
     * @implNote
     *             1. if any end of edge contains a non-existing node, this operation will be rejected.
     *             2. if the new edge contains already-registered alphabet in the node (e.g. other edge
     *             occupies the alphabet), this operation will be rejected.
     *             3. If the new edge contains alphabet that is not registered in this DFA, this operation
     *             will be rejected.
     * @return whether successfully added or not.
     */
    public boolean registerEdge(DFAEdge edge) {
        assert edge != null && edge.getTail() != null;
        // check if the new alphabet is a subset of the general alphabet set
        if (!alphabetSet.containsAll(edge.getAlphabet())) return false;
        // register edge to the tail node
        if (edge.getTail().registerEdge(edge)) {
            edgeSet.add(edge);
            return true;
        }
        return false;
    }

    /**
     * Register a node to the DFA.
     *
     * @param node new node
     * @return successfully added or not.
     */
    public boolean registerNode(DFANode node) {
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
     * Add a new set of strings to the general alphabet set of the DFA.
     *
     * @param alphabetSet new alphabet set
     */
    public void registerAlphabet(Set<String> alphabetSet) {
        assert alphabetSet != null;
        this.alphabetSet.addAll(alphabetSet);
    }

    /**
     * Remove the given edge.
     *
     * @param edge edge to be deleted
     * @return whether the edge is successfully removed
     */
    public boolean removeEdge(DFAEdge edge) {
        assert edge != null && edge.getTail() != null;
        if (edge.getTail().removeEdge(edge)) {
            edgeSet.remove(edge);
            return true;
        }
        return false;
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

        // find the subset of relevant edges
        final Set<DFAEdge> removalSubset = edgeSet.stream()
                .filter((DFAEdge e) -> e.getTail() == node || e.getHead() == node)
                .collect(Collectors.toSet());
        // notify these relevant nodes that these edges will be removed
        removalSubset.forEach(e -> e.getTail().removeEdge(e));
        // remove these edges
        edgeSet.removeAll(removalSubset);
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
        final DFANode nextNode = peek(input);
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
