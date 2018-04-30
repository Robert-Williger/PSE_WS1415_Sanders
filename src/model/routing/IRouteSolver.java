package model.routing;

import java.util.List;

import model.Progressable;

public interface IRouteSolver extends Progressable {

    Route calculateRoute(List<InterNode> edges);

}