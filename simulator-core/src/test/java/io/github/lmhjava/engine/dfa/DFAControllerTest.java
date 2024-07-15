package io.github.lmhjava.engine.dfa;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test for DFA Controller
public class DFAControllerTest {

    private DFAController controller;

    @BeforeEach
    public void init() {
        controller = new DFAController();
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

    // Test
}
