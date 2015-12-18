package model.routing;

import java.util.List;

public class Path {
    private final int length;
    private final List<Integer> edges;
    private final InterNode startNode;
    private final InterNode endNode;

    public Path(final int length, final List<Integer> edges, final InterNode start, final InterNode end) {
        this.length = length;
        this.edges = edges;
        startNode = start;
        endNode = end;
    }

    public int getLength() {
        return length;
    }

    public List<Integer> getEdges() {
        return edges;
    }

    public InterNode getStartNode() {
        return startNode;
    }

    public InterNode getEndNode() {
        return endNode;
    }

}