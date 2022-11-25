package sample0.junit4;

import org.junit.Test;
import sample0.ControlDomination;

public class ControlDominationTest {
    ControlDomination controlDomination = new ControlDomination();
    @Test
    public void testIfStmtControlDomination() {
        controlDomination.ifStmtControlDom();
    }

    @Test
    public void testForLoopControlDomination() {
        controlDomination.forLoopControlDom();
    }

    @Test
    public void testWhileLoopControlDomination() {
        controlDomination.whileLoopControlDom();
    }
}
