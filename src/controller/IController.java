package controller;

import view.IView;

public interface IController<V extends IView> {

    void setView(V view);

}