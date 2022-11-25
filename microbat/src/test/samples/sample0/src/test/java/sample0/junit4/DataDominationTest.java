package sample0.junit4;

import org.junit.Test;
import sample0.DataDomination;

public class DataDominationTest {
    DataDomination dataDomination = new DataDomination();
    @Test
    public void testArrayDataDomination() {
       dataDomination.arrayDataDom();
    }

    @Test
    public void testForLoopDataDomination() {
        dataDomination.forLoopDataDom();
    }

    @Test
    public void testWhileLoopDataDomination() {
        dataDomination.whileLoopDataDom();
    }

    @Test
    public void testAssignmentDataDomination() {
        dataDomination.assignmentDataDom();
    }
}
