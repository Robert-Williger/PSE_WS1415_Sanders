package model.renderEngine;

public interface ICache<K, V> {

    void clear();

    void setSize(int size);

    int getSize();

    void put(K key, V value);

    V get(K key);

    boolean contains(long id);

}