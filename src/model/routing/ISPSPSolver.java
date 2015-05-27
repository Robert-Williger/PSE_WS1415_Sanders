package model.routing;

public interface ISPSPSolver extends Progressable {

    Path calculateShortestPath(InterNode start, InterNode end);

}