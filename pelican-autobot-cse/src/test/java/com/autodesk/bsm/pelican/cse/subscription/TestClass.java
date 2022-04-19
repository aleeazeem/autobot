package com.autodesk.bsm.pelican.cse.subscription;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TestClass {

    public static void main(String[] args) {
        Map<String, Integer> unsortMap = new HashMap<>();
        unsortMap.put("B", 5);
        unsortMap.put("A", 8);
        unsortMap.put("D", 2);
        unsortMap.put("C", 7);

        System.out.println("Before sorting......");
        System.out.println(unsortMap);

        Map<String, Integer> sortedMapAsc = Util.sortByComparator(unsortMap);
        System.out.println(sortedMapAsc);
    }
}

class MyComparator implements Comparator<Entry<String, Integer>> {
    public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
        return o1.getValue().compareTo(o2.getValue());
    }
}

class Util {
    public static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {

        List<Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        Collections.sort(list, new MyComparator());

        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}
