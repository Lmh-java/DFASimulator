package io.github.lmhjava.engine.dfa;


import io.github.lmhjava.engine.edge.DFAEdge;
import io.github.lmhjava.engine.exception.NextNodeUndefException;
import io.github.lmhjava.engine.node.DFANode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        controller.addNode(testNode1);
        controller.addNode(testNode2);
        controller.addEdge(testEdge);
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

    // Test addEdge
    @Test
    public void testAddEdge() {
        assertThrows(AssertionError.class, () -> controller.addEdge(null));
        // Rejected, head and tail node do not exist.
        assertFalse(controller.addEdge(testEdge));
        controller.addNode(testNode1);

        // Rejected, head node does not exist.
        assertFalse(controller.addEdge(testEdge));
        controller.addNode(testNode2);

        // Rejected, alphabet is not registered into the DFA.
        assertFalse(controller.addEdge(testEdge));
        controller.registerAlphabet("A");

        // Accepted
        assertTrue(controller.addEdge(testEdge));
        assertTrue(controller.getEdgeSet().contains(testEdge));
        // Check the tail node.
        assertTrue(testNode1.getEdges().contains(testEdge));

        // Add another edge with overlaps
        DFAEdge newEdge = new DFAEdge(testNode1, testNode2);
        newEdge.addAlphabet("B");
        // Rejected, alphabet is not registered into the DFA.
        assertFalse(controller.addEdge(newEdge));
        controller.registerAlphabet("B");

        // Accepted and Merged
        assertTrue(controller.addEdge(newEdge));
        assertFalse(controller.getEdgeSet().contains(newEdge));
        assertEquals(2, testEdge.getAlphabet().size());
        // Check the tail node.
        assertTrue(testNode1.getEdges().contains(testEdge));
    }

    // Test addNode
    @Test
    public void testAddNode() {
        assertFalse(controller.addNode(null));
        assertTrue(controller.addNode(testNode1));
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
        controller.removeEdge(testEdge);
        assertFalse(controller.getEdgeSet().contains(testEdge));
        assertFalse(testNode1.getEdges().contains(testEdge));
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
        controller.addEdge(newEdge);

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
        controller.addEdge(newEdge);

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
