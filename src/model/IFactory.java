package model;

@FunctionalInterface
public interface IFactory<T> {

    T create();

}
