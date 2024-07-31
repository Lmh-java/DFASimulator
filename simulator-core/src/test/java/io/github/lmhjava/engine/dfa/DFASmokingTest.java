package io.github.lmhjava.engine.dfa;

import io.github.lmhjava.engine.exception.NextNodeUndefException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Smoking test to spot problems quickly
public class DFASmokingTest {

    private DFAController controller;

    @BeforeEach
    public void init() {
        controller = new DFAController();
    }

    @Test
    public void testDynamicallyUpdates() throws NextNodeUndefException {
        controller.registerAlphabet("A");
        controller.registerAlphabet("B");
        DFANode node1 = new DFANode("NODE1");
        DFANode node2 = new DFANode("NODE2");
        controller.registerNode(node1);
        controller.registerNode(node2);
        DFAEdge edge = new DFAEdge(node1, node2);
        controller.registerEdge(edge);
        controller.setInitialNode(node1);

        assertThrows(NextNodeUndefException.class, () -> controller.next("C"));
        assertThrows(NextNodeUndefException.class, () -> controller.next("A"));
        edge.registerAlphabet("A");
        assertEquals(node2, controller.next("A"));

        controller.reset();
        DFAEdge edgeBack = new DFAEdge(node2, node1);
        edgeBack.registerAllAlphabet("A", "B", "C");
        assertFalse(controller.registerEdge(edgeBack));
        assertEquals(node2, controller.next("A"));
        assertThrows(NextNodeUndefException.class, () -> controller.next("C"));
        assertThrows(NextNodeUndefException.class, () -> controller.next("B"));
        edgeBack.unregisterAlphabet("C");
        assertTrue(controller.registerEdge(edgeBack));
        assertEquals(node1, controller.next("B"));

        controller.reset();
        edgeBack.unregisterAlphabet("A");
        assertEquals(node2, controller.next("A"));
        assertThrows(NextNodeUndefException.class, () -> controller.next("A"));
        assertEquals(node1, controller.next("B"));
    }

    @Test
    public void testRegisterAndRemoveAlphabets() throws NextNodeUndefException {
        controller.registerAlphabet("A");
        DFANode node1 = new DFANode("NODE1");
        DFANode node2 = new DFANode("NODE2");
        DFANode node3 = new DFANode("NODE3");
        DFAEdge edge12 = new DFAEdge(node1, node2, "A");
        DFAEdge edge13 = new DFAEdge(node2, node3, "A");
        DFAEdge edge23 = new DFAEdge(node3, node1, "A");
        controller.registerNode(node1);
        controller.registerNode(node2);
        controller.registerNode(node3);
        controller.registerEdge(edge12);
        controller.registerEdge(edge13);
        controller.registerEdge(edge23);
        controller.setInitialNode(node1);

        assertEquals(3, controller.getNodeSet().size());
        assertEquals(3, controller.getEdgeSet().size());
        controller.registerAlphabet("B");
        edge12.registerAlphabet("B");
        assertEquals(node2, controller.next("B"));
        controller.reset();

        controller.unregisterAlphabet("B");
        assertThrows(NextNodeUndefException.class, () -> controller.next("B"));
        assertEquals(1, edge12.getAlphabets().size());

        controller.reset();
        edge12.setElseEdge(true);
        edge12.unregisterAlphabet("A");
        assertEquals(node2, controller.next("B"));
    }
}
