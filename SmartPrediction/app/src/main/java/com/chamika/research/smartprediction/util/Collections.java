package com.chamika.research.smartprediction.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Collections {
    public static <K, V extends Comparable> Map<K, V> sortByValue(Map<K, V> source) {
        List<Map.Entry<K, V>> list = new LinkedList<>(source.entrySet());

        java.util.Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return o2.getValue().compareTo(o1.getValue()); //descending
            }
        });

        HashMap<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> data : list) {
            sortedMap.put(data.getKey(), data.getValue());
        }
        return sortedMap;
    }
}
