package model.routing;

import java.util.Iterator;

public interface IGraph {

    int getNodes();

    int getEdges();

    Iterator<Integer> getAdjacentNodes(int node);

    int getFirstNode(long edge);

    int getSecondNode(long edge);

    long getEdge(int node1, int node2);

    int getWeight(long edge);

}