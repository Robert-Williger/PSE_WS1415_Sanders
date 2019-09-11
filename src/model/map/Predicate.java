package model.map;

public interface Predicate {

    boolean test(int id, int zoom, boolean leaf);

    default boolean cutOffTrees() {
        return false;
    }
}
