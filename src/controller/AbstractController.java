package controller;

import view.IView;

public class AbstractController<V extends IView> implements IController<V> {

    @Override
    public void setView(final V view) {

    }

}
