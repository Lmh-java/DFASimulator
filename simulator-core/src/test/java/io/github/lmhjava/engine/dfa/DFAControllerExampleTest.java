package io.github.lmhjava.engine.dfa;

import io.github.lmhjava.engine.edge.DFAEdge;
import io.github.lmhjava.engine.exception.NextNodeUndefException;
import io.github.lmhjava.engine.node.DFANode;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

// Examples tests for controllers
public class DFAControllerExampleTest {

    // A - (a) -> B - (a) -> A
    @Test
    public void testBidirectionalTransition() throws NextNodeUndefException {
        DFANode node1 = new DFANode("A");
        DFANode node2 = new DFANode("B");
        DFAEdge edge1 = new DFAEdge(node1, node2, "a");
        DFAEdge edge2 = new DFAEdge(node2, node1, "a");
        DFAController controller = new DFAController();
        controller.registerAlphabet("a");
        controller.registerNode(node1);
        controller.registerNode(node2);
        controller.setInitialNode(node1);
        controller.registerEdge(edge1);
        controller.registerEdge(edge2);

        assertNull(controller.getCurrentNode());
        for (int i = 0; i < 10; i++) {
            assertEquals(node2, controller.next("a"));
            assertEquals(node1, controller.next("a"));
        }
    }

    //    / (a, b, c) - \
    // A -                -> B - (e, f, g, h) -> A
    //    \ (d, e, f) - /
    @Test
    public void testMultipleBranchesToOne() throws NextNodeUndefException {
        DFANode node1 = new DFANode("A");
        DFANode node2 = new DFANode("B");
        DFAEdge edge1 = new DFAEdge(node1, node2);
        edge1.addAlphabet("a");
        edge1.addAlphabet("b");
        edge1.addAlphabet("c");
        DFAEdge edge2 = new DFAEdge(node1, node2);
        edge2.addAlphabet("d");
        edge2.addAlphabet("e");
        edge2.addAlphabet("f");
        DFAEdge edge3 = new DFAEdge(node2, node1);
        edge3.addAlphabet("e");
        edge3.addAlphabet("f");
        edge3.addAlphabet("g");
        edge3.addAlphabet("h");
        DFAController controller = new DFAController();
        controller.registerAlphabet(Set.of("a", "b", "c", "d", "e", "f", "g", "h"));
        controller.registerNode(node1);
        controller.registerNode(node2);
        controller.registerEdge(edge1);
        controller.registerEdge(edge2);
        controller.registerEdge(edge3);
        controller.setInitialNode(node1);
        assertThrows(NextNodeUndefException.class, () -> controller.next("g"));
        assertEquals(node2, controller.next("a"));
        assertThrows(NextNodeUndefException.class, () -> controller.next("b"));
        assertEquals(node1, controller.next("f"));
        assertEquals(node2, controller.next("f"));
    }

    // A - (a) -> A
    @Test
    public void testSelfLoop() throws NextNodeUndefException {
        DFANode node = new DFANode("A");
        DFAEdge edge = new DFAEdge(node, node, "a");
        DFAController controller = new DFAController();
        controller.registerAlphabet("a");
        controller.registerNode(node);
        controller.registerEdge(edge);
        controller.setInitialNode(node);
        assertThrows(NextNodeUndefException.class, () -> controller.next("b"));
        assertEquals(node, controller.next("a"));
        assertEquals(node, controller.next("a"));
        assertEquals(node, controller.next("a"));
    }

    // A - (a) -> B - (a) -> A
    //   \
    //     - (ELSE) -> A
    @Test
    public void testElseEdges() throws NextNodeUndefException {
        DFANode node1 = new DFANode("A");
        DFANode node2 = new DFANode("B");
        DFAEdge edge1 = new DFAEdge(node1, node2, "a");
        DFAEdge elseEdge = new DFAEdge(node1, node1);
        elseEdge.setElseEdge(true);
        DFAEdge edge2 = new DFAEdge(node2, node1, "a");
        DFAController controller = new DFAController();
        controller.registerAlphabet("a");
        controller.registerAlphabet("b");
        controller.registerAlphabet("c");
        controller.registerNode(node1);
        controller.registerNode(node2);
        controller.setInitialNode(node1);
        controller.registerEdge(edge1);
        controller.registerEdge(edge2);
        controller.registerEdge(elseEdge);

        assertNull(controller.getCurrentNode());
        for (int i = 0; i < 10; i++) {
            assertEquals(node1, controller.next("b"));
            assertEquals(node1, controller.next("c"));
            assertEquals(node2, controller.next("a"));
            assertEquals(node1, controller.next("a"));
        }
    }
}
