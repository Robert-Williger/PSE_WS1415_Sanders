package model.routing;

import java.util.Iterator;

public interface IDirectedGraph {

    int getNodes();

    int getEdges();

    Iterator<Integer> getOutgoingEdges(int node);

    int getEndNode(int edge);

    int getStartNode(int edge);

    int getWeight(int edge);

}
