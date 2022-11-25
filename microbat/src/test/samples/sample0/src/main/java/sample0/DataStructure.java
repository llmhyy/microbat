package sample0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataStructure {
    public void runArray() {
        int arr[] = new int[] {0, 1, 5, 3, 4};
        for (int i = 0; i < arr.length; i++) {
            int b = arr[i];
        }
        arr[2] = 2;
    }

    public void runArrayList() {
        List<Integer> list = new ArrayList<>();
        list.add(0);
        list.add(2);
        list.add(2);
        for (int i = 0; i < list.size(); i++) {
            int b = list.get(i);
        }
        list.remove(2);
        list.set(1, 1);
    }

    public void runHashSet() {
        Set<Integer> set = new HashSet<>();
        set.add(0);
        set.add(1);
        set.add(2);
        Iterator<Integer> it = set.iterator();
        while (it.hasNext()) {
            int b = it.next();
        }
        set.remove(2);
        set.clear();
    }

    public void runHashMap() {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, true);
        map.put(1, false);
        map.put(2, true);
        Iterator<Integer> it = map.keySet().iterator();
        while (it.hasNext()) {
            int b = it.next();
            boolean value = map.get(b);
        }
        map.remove(1);
        map.clear();
    }
}
