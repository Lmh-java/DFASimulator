package io.github.lmhjava.engine.dfa;


import io.github.lmhjava.engine.exception.NextNodeUndefException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Test for DFA Controller
public class DFAControllerTest {

    private DFAController controller;
    private DFANode testNode1;
    private DFANode testNode2;
    private DFAEdge testEdge;

    @BeforeEach
    public void init() {
        controller = new DFAController();
        testNode1 = new DFANode("Test 1");
        testNode2 = new DFANode("Test 2");
        // constructs testNode1 - A -> testNode2
        testEdge = new DFAEdge(testNode1, testNode2);
        testEdge.addAlphabet("A");
    }

    private void initDFA() {
        controller.registerAlphabet("A");
        controller.registerNode(testNode1);
        controller.registerNode(testNode2);
        controller.registerEdge(testEdge);
    }

    // Test constructor
    @Test
    public void testDFAController() {
        assertTrue(controller.getAlphabetSet().isEmpty());
        assertTrue(controller.getEdgeSet().isEmpty());
        assertTrue(controller.getNodeSet().isEmpty());
        assertNull(controller.getCurrentNode());
        assertNull(controller.getInitialNode());
    }

    // Test registerEdge
    @Test
    public void testRegisterEdge() {
        assertThrows(AssertionError.class, () -> controller.registerEdge(null));
        // Rejected, head and tail node do not exist.
        assertFalse(controller.registerEdge(testEdge));
        controller.registerNode(testNode1);

        // Rejected, head node does not exist.
        assertFalse(controller.registerEdge(testEdge));
        controller.registerNode(testNode2);

        // Rejected, alphabet is not registered into the DFA.
        assertFalse(controller.registerEdge(testEdge));
        controller.registerAlphabet("A");

        // Accepted
        assertTrue(controller.registerEdge(testEdge));
        assertTrue(controller.getEdgeSet().contains(testEdge));
        // Check the tail node.
        assertTrue(testNode1.getEdges().contains(testEdge));

        // Add another edge with overlaps
        DFAEdge newEdge = new DFAEdge(testNode1, testNode2);
        newEdge.addAlphabet("B");
        // Rejected, alphabet is not registered into the DFA.
        assertFalse(controller.registerEdge(newEdge));
        controller.registerAlphabet("B");

        // Accepted
        assertTrue(controller.registerEdge(newEdge));
        // Check the tail node.
        assertTrue(testNode1.getEdges().contains(testEdge));
    }

    // Test registerEdge with duplicate keys
    @Test
    public void testRegisterEdgeDuplicateKeys() {
        initDFA();
        // Try to add an edge to testNode1 with duplicate keys with the original edge.
        DFAEdge newEdge = new DFAEdge(testNode1, testNode2);
        controller.registerAlphabet("A");
        controller.registerAlphabet("B");
        controller.registerAlphabet("C");
        newEdge.addAllAlphabet(Set.of("A", "B", "C"));
        assertFalse(controller.registerEdge(newEdge));
        assertEquals(1, controller.getEdgeSet().size());
        controller.removeEdge(newEdge);

        newEdge.setAlphabet(Set.of("B", "C"));
        assertTrue(controller.registerEdge(newEdge));
        assertEquals(2, controller.getEdgeSet().size());
    }

    // Test addNode
    @Test
    public void testRegisterNode() {
        assertFalse(controller.registerNode(null));
        assertTrue(controller.registerNode(testNode1));
        assertTrue(controller.getNodeSet().contains(testNode1));
    }

    // Test registerAlphabet
    @Test
    public void testRegisterAlphabet() {
        assertTrue(controller.registerAlphabet("A"));
        assertFalse(controller.registerAlphabet("A"));
        assertEquals(1, controller.getAlphabetSet().size());
        assertTrue(controller.getAlphabetSet().contains("A"));
    }

    // Test setCurrentNode
    @Test
    public void testSetCurrentNode() {
        initDFA();
        assertFalse(controller.setCurrentNode(null));
        assertFalse(controller.setCurrentNode(new DFANode("Test 3")));

        assertTrue(controller.setCurrentNode(testNode1));
        assertTrue(testNode1.isOnCurrentState());
        assertFalse(testNode2.isOnCurrentState());
        assertEquals(testNode1, controller.getCurrentNode());

        assertTrue(controller.setCurrentNode(testNode2));
        assertTrue(testNode2.isOnCurrentState());
        assertFalse(testNode1.isOnCurrentState());
        assertEquals(testNode2, controller.getCurrentNode());
    }

    // Test removeEdge
    @Test
    public void testRemoveEdge() {
        initDFA();
        assertTrue(controller.removeEdge(testEdge));
        assertFalse(controller.getEdgeSet().contains(testEdge));
        assertFalse(testNode1.getEdges().contains(testEdge));
    }

    // Test removeEdge with else edge
    @Test
    public void testRemoveEdgeWithElse() {
        initDFA();
        DFAEdge elseEdge = new DFAEdge(testNode1, testNode2);
        elseEdge.addAlphabet("a");
        elseEdge.addAlphabet("b");
        elseEdge.setElseEdge(true);
        controller.registerAlphabet(Set.of("a", "b"));
        controller.registerEdge(elseEdge);
        assertEquals(2, controller.getEdgeSet().size());
        assertEquals(elseEdge, testNode1.getElseEdge());

        controller.removeEdge(testEdge);
        assertEquals(1, controller.getEdgeSet().size());
        assertEquals(elseEdge, testNode1.getElseEdge());
    }

    // Test remove else edge
    @Test
    public void testRemoveElseEdge() {
        initDFA();
        DFAEdge elseEdge = new DFAEdge(testNode1, testNode2);
        elseEdge.setElseEdge(true);
        controller.registerEdge(elseEdge);
        assertEquals(2, controller.getEdgeSet().size());
        assertEquals(elseEdge, testNode1.getElseEdge());
        controller.removeEdge(elseEdge);
        assertEquals(1, controller.getEdgeSet().size());
        assertNull(testNode1.getElseEdge());
    }

    // Test removeNode
    @Test
    public void testRemoveNode() {
        controller.removeNode(testNode1);
        initDFA();
        assertThrows(AssertionError.class, () -> controller.removeNode(null));
        assertTrue(controller.getNodeSet().contains(testNode1));
        assertTrue(controller.getEdgeSet().contains(testEdge));
        controller.removeNode(testNode1);
        assertFalse(controller.getNodeSet().contains(testNode1));
        assertFalse(controller.getEdgeSet().contains(testEdge));
    }

    // Test next
    @Test
    public void testNext() throws NextNodeUndefException {
        initDFA();
        assertThrows(AssertionError.class, () -> controller.next(null));
        assertThrows(NextNodeUndefException.class, () -> controller.next("A"));
        controller.setInitialNode(testNode1);
        assertEquals(testNode2, controller.next("A"));
        assertFalse(testNode1.isOnCurrentState());
        assertTrue(testNode2.isOnCurrentState());
        assertEquals(testNode2, controller.getCurrentNode());

        // build another connection from node 2 to node 1
        DFAEdge newEdge = new DFAEdge(testNode2, testNode1);
        newEdge.addAlphabet("B");
        controller.registerAlphabet("B");
        controller.registerEdge(newEdge);

        assertThrows(NextNodeUndefException.class, () -> controller.next("A"));
        assertEquals(testNode1, controller.next("B"));
        assertTrue(testNode1.isOnCurrentState());
        assertFalse(testNode2.isOnCurrentState());
        assertEquals(testNode1, controller.getCurrentNode());
    }

    // Test peek
    @Test
    public void testPeek() throws NextNodeUndefException {
        initDFA();
        assertThrows(AssertionError.class, () -> controller.next(null));
        assertThrows(NextNodeUndefException.class, () -> controller.peek("A"));
        controller.setInitialNode(testNode1);
        assertEquals(testNode2, controller.peek("A"));
        assertFalse(testNode1.isOnCurrentState());
        assertFalse(testNode2.isOnCurrentState());
        assertNull(controller.getCurrentNode());

        // build another connection from node 2 to node 1
        DFAEdge newEdge = new DFAEdge(testNode2, testNode1);
        newEdge.addAlphabet("B");
        controller.registerAlphabet("B");
        controller.registerEdge(newEdge);

        assertEquals(testNode2, controller.peek("A"));
        assertThrows(NextNodeUndefException.class, () -> controller.peek("B"));
        assertFalse(testNode1.isOnCurrentState());
        assertFalse(testNode2.isOnCurrentState());
        assertNull(controller.getCurrentNode());
    }

    // Test cloneDFA
    @Test
    public void testCloneDFA() {
        initDFA();
        controller.setInitialNode(testNode1);
        controller.setCurrentNode(testNode2);

        DFAController clone = controller.cloneDFA();
        assertIterableEquals(controller.getEdgeSet(), clone.getEdgeSet());
        assertIterableEquals(controller.getNodeSet(), clone.getNodeSet());
        assertIterableEquals(controller.getAlphabetSet(), clone.getAlphabetSet());
        assertEquals(controller.getInitialNode(), clone.getInitialNode());
        assertNotEquals(controller.getCurrentNode(), clone.getCurrentNode());
    }
}
