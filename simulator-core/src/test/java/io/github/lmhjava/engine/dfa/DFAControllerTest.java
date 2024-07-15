package io.github.lmhjava.engine.dfa;


import io.github.lmhjava.engine.edge.DFAEdge;
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

    // Test constructor
    @Test
    public void testDFAController() {
        assertTrue(controller.getAlphabetSet().isEmpty());
        assertTrue(controller.getEdgeList().isEmpty());
        assertTrue(controller.getNodeList().isEmpty());
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
        controller.addAlphabet("A");

        // Accepted
        assertTrue(controller.addEdge(testEdge));
        assertTrue(controller.getEdgeList().contains(testEdge));
        // Check the tail node.
        assertTrue(testNode1.getEdges().contains(testEdge));

        // Add another edge with overlaps
        DFAEdge newEdge = new DFAEdge(testNode1, testNode2);
        newEdge.addAlphabet("B");
        // Rejected, alphabet is not registered into the DFA.
        assertFalse(controller.addEdge(newEdge));
        controller.addAlphabet("B");

        // Accepted and Merged
        assertTrue(controller.addEdge(newEdge));
        assertFalse(controller.getEdgeList().contains(newEdge));
        assertEquals(2, testEdge.getAlphabet().size());
        // Check the tail node.
        assertTrue(testNode1.getEdges().contains(testEdge));
    }

    // Test addNode
    @Test
    public void testAddNode() {
        assertFalse(controller.addNode(null));
        assertTrue(controller.addNode(testNode1));
        assertTrue(controller.getNodeList().contains(testNode1));
    }

    // Test addAlphabet
    @Test
    public void testAddAlphabet() {
        assertTrue(controller.addAlphabet("A"));
        assertFalse(controller.addAlphabet("A"));
        assertEquals(1, controller.getAlphabetSet().size());
        assertTrue(controller.getAlphabetSet().contains("A"));
    }
}
