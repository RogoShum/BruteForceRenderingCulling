package rogo.renderingculling.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.BiConsumer;

public class IndexedSet<E> {
    private final ArrayList<E> list;
    private final HashSet<E> set;

    public IndexedSet() {
        list = new ArrayList<>();
        set = new HashSet<>();
    }

    public boolean add(E element) {
        if (set.add(element)) {
            list.add(element);
            return true;
        }
        return false;
    }

    public boolean remove(E element) {
        if (set.remove(element)) {
            list.remove(element);
            return true;
        }
        return false;
    }

    public void forEach(BiConsumer<? super E, Integer> action) {
        for (int i = 0; i < list.size(); ++i) {
            action.accept(list.get(i), i);
        }
    }

    public E get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    public boolean contains(E element) {
        return set.contains(element);
    }

    public void clear() {
        list.clear();
        set.clear();
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
