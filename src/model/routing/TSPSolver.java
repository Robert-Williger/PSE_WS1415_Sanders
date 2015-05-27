package model.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TSPSolver extends AbstractComplexRouteSolver {
    private final IGraph graph;
    private IGraph mst;
    private boolean[] mstMarked;
    private HashMap<Long, Path> mapping;
    private JPMST jp;
    private List<Integer> tspNodes;
    private int state;
    private final int[] progress;
    private final ISPSPSolver solver;

    public TSPSolver(final IGraph graph) {
        this.graph = graph;
        progress = new int[3];
        solver = createSPSPSolver();
        solver.addProgressListener(new IProgressListener() {
            @Override
            public void progressDone(final int i) {
                fireProgressDone(progress[0]);
            }

            @Override
            public void errorOccured(final String message) {
                fireErrorOccured(message);
            }
        });
    }

    public IAddressablePriorityQueue<Integer> createQueue() {
        return new AddressableBinaryHeap<Integer>();
    }

    @Override
    public List<Path> calculateRoute(final List<InterNode> edges) {
        mapping = new HashMap<Long, Path>();
        tspNodes = new ArrayList<Integer>();
        // 0 working, 1 ready, 2 cancel/error
        state = 0;
        fireProgressDone(-1);

        progress[0] = (edges.size() * edges.size() - edges.size()) / 2;
        progress[0] = 96 / progress[0];
        progress[1] = 2;
        progress[2] = 100;

        final IGraph completeGraph = createCompleteGraph(edges);

        final List<Path> ret = new ArrayList<Path>();
        if (state == 0) {
            jp = new JPMST(completeGraph);
            mst = jp.calculateMST();
            fireProgressDone(progress[1]);

            // deep-first search
            mstMarked = new boolean[completeGraph.getNodes()];
            mstMarked[0] = true;
            tspNodes.add(0);
            dfs(0, 0);

            final int num = tspNodes.size();
            for (int i = 1; i < num; i++) {

                final long e = graph.getEdge(tspNodes.get(i - 1), tspNodes.get(i));

                // long e = graph.getEdge(i - 1, i);
                ret.add(mapping.get(e));
            }

            ret.add(mapping.get(graph.getEdge(tspNodes.get(num - 1), 0)));
            fireProgressDone(progress[2]);
        }
        return ret;
    }

    private void dfs(final int u, final int v) {

        final Iterator<Integer> it = mst.getAdjacentNode(v);
        while (it.hasNext()) {
            final int w = it.next();
            if (!mstMarked[w]) {
                tspNodes.add(w);
                mstMarked[w] = true;
                dfs(v, w);
            }
        }
    }

    private IGraph createCompleteGraph(final List<InterNode> points) {
        final List<Long> edges = new ArrayList<Long>();
        final List<Integer> weights = new ArrayList<Integer>();
        for (int u = 0; u < points.size() && state == 0; u++) {
            for (int v = (u + 1); v < points.size() && state == 0; v++) {
                final long edge = graph.getEdge(u, v);
                edges.add(edge);
                final Path path = solver.calculateShortestPath(points.get(u), points.get(v));
                if (path == null) {
                    state = 2;
                } else {
                    weights.add(path.getLength());
                    mapping.put(edge, path);
                }
            }
        }
        return state == 0 ? new Graph(points.size(), edges, weights) : null;
    }

    @Override
    public void cancelCalculation() {
        solver.cancelCalculation();
        state = 2;
    }

    @Override
    protected ISPSPSolver createSPSPSolver() {
        return new Dijkstra(graph);
    }

}