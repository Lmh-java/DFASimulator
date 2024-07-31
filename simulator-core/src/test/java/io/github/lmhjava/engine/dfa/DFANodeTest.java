package io.github.lmhjava.engine.dfa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DFANodeTest {

    private DFANode testNode;

    @BeforeEach
    public void init() {
        testNode = new DFANode("test content");
    }

    @Test
    public void testUpdateEventTriggered() {
        testNode.setOnCurrentStateUpdate(() -> testNode.setContent("ABC"));
        assertEquals("test content", testNode.getContent());
        testNode.setOnCurrentState(true);
        assertEquals("ABC", testNode.getContent());
    }
}
