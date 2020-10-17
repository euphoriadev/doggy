package ru.euphoria.doggy.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HashMultiset<E> extends ConcurrentHashMap<E, Integer> {
    public HashMultiset() {

    }

    public int count(E item) {
        return get(item);
    }

    public void add(E item) {
        Integer count = get(item);
        put(item, count == null ? 1 : count + 1);
    }

    public List<Pair<E, Integer>> copyByCount() {
        List<Pair<E, Integer>> list = copyAsList();
        Collections.sort(list, (o1, o2) -> Integer.compare(o2.second, o1.second));
        return list;
    }

    public List<Pair<E, Integer>> copyAsList() {
        List<Pair<E, Integer>> list = new ArrayList<>(size());
        for (Entry<E, Integer> entry : entrySet()) {
            E key = entry.getKey();
            Integer value = entry.getValue();
            list.add(Pair.create(key, value == null ? 0 : value));
        }
        return list;
    }
}
