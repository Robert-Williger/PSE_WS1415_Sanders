package util;

import java.util.List;

public interface IAddressablePriorityQueue<T> {

    void addAll(List<T> elements, List<Integer> key);

    void addAll(List<T> elements);

    boolean isEmpty();

    int size();

    void insert(T element, int key);

    void insert(T element);

    T min();

    T deleteMin();

    boolean remove(T element);

    void changeKey(T element, int key);

    void merge(IAddressablePriorityQueue<T> queue);

    boolean contains(T element);

}