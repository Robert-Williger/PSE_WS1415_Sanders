package model.routing;

import java.util.List;

public interface IRouteSolver extends Progressable {

    Route calculateRoute(List<InterNode> edges);

}