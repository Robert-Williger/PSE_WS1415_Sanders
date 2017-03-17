package model.routing;

import java.util.PrimitiveIterator.OfInt;

public interface IUndirectedGraph {

    int getNodes();

    int getEdges();

    OfInt getAdjacentNodes(int node);

    long getEdge(int firstNode, int secondNode);

    int getFirstNode(long edge);

    int getSecondNode(long edge);

    int getWeight(long edge);

}