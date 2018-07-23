package util;

import java.util.List;

public interface IAddressablePriorityQueue<T> {

    void addAll(List<T> elements, List<Double> key);

    void addAll(List<T> elements);

    boolean isEmpty();

    int size();

    void insert(T element, double key);

    void insert(T element);

    T min();

    T deleteMin();

    boolean remove(T element);

    void changeKey(T element, double key);

    void merge(IAddressablePriorityQueue<T> queue);

    boolean contains(T element);

    void clear();

}