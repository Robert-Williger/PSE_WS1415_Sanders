package model.routing;

import java.util.List;

public interface IComplexRouteSolver extends Progressable {

    List<Path> calculateRoute(List<InterNode> edges);

}