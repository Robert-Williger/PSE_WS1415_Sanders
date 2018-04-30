package model.routing;

import model.Progressable;

public interface ISPSPSolver extends Progressable {

    Path calculateShortestPath(InterNode start, InterNode end);

}