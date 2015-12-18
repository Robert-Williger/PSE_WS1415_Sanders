package model.routing;

import java.util.Iterator;

public interface IUndirectedGraph {

    int getNodes();

    int getEdges();

    Iterator<Integer> getAdjacentNodes(int node);

    long getEdge(int firstNode, int secondNode);

    int getFirstNode(long edge);

    int getSecondNode(long edge);

    int getWeight(long edge);

}