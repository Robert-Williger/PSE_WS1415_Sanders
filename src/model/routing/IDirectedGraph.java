package model.routing;

import java.util.PrimitiveIterator.OfInt;

public interface IDirectedGraph {

    int getNodes();

    int getEdges();

    OfInt getOutgoingEdges(int node);

    int getEndNode(int edge);

    int getStartNode(int edge);

    int getWeight(int edge);

}
