package sample0.junit5;

import org.junit.Test;
import sample0.DataStructure;

public class DataStructureTest {
    DataStructure dataStructure = new DataStructure();

    @Test
    public void testRunArray() {
        dataStructure.runArray();
    }

    @Test
    public void testRunArrayList() {
        dataStructure.runArrayList();
    }

    @Test
    public void testRunHashMap() {
        dataStructure.runHashMap();
    }

    @Test
    public void testRunHashSet() {
        dataStructure.runHashSet();
    }
}
