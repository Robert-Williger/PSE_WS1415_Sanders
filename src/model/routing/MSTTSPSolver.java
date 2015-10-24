package model.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import model.IProgressListener;

public class MSTTSPSolver extends AbstractComplexRouteSolver {
    private IGraph mst;
    private HashMap<Long, Path> mapping;
    private List<Integer> tspNodes;
    private boolean canceled;
    private final ISPSPSolver solver;

    public MSTTSPSolver(final IGraph graph) {
        super(graph);

        solver = createSPSPSolver();
        solver.addProgressListener(new IProgressListener() {

            @Override
            public void progressDone(final int i) {

            }

            @Override
            public void errorOccured(final String message) {
                fireErrorOccured(message);
            }

            @Override
            public void stepCommenced(String step) {

            }
        });
    }

    public IAddressablePriorityQueue<Integer> createQueue() {
        return new AddressableBinaryHeap<Integer>();
    }

    @Override
    public List<Path> calculateRoute(final List<InterNode> edges) {
        fireProgressDone(-1);

        mapping = new HashMap<Long, Path>();

        canceled = false;

        final IGraph completeGraph = createCompleteGraph(edges);

        final List<Path> ret = new ArrayList<Path>();

        if (!canceled) {
            mst = new JPMST(completeGraph).calculateMST();
            final int num = mst.getNodes();

            List<Integer> minRoute = null;
            int minRouteLength = Integer.MAX_VALUE;

            for (int i = 0; i < completeGraph.getNodes(); i++) {
                tspNodes = new ArrayList<Integer>();
                tspNodes.add(i);
                dfs(i, i);

                int weightSum = 0;
                for (int j = 0; j < num; j++) {
                    final long e = graph.getEdge(tspNodes.get(j), tspNodes.get((j + 1) % num));
                    weightSum += mapping.get(e).getLength();
                }

                if (weightSum < minRouteLength) {
                    minRoute = tspNodes;
                    minRouteLength = weightSum;
                }
            }

            for (int j = 0; j < num; j++) {
                final long e = graph.getEdge(minRoute.get(j), minRoute.get((j + 1) % num));
                ret.add(mapping.get(e));
            }
        }

        return ret;
    }

    private void dfs(final int u, final int v) {

        final Iterator<Integer> it = mst.getAdjacentNodes(v);
        while (it.hasNext()) {
            final int w = it.next();
            if (w != u) {
                tspNodes.add(w);
                dfs(v, w);
            }
        }
    }

    private IGraph createCompleteGraph(final List<InterNode> points) {
        final int size = points.size() - 1;
        final long[] edges = new long[size * (size + 1) / 2];
        final int[] weights = new int[edges.length];

        final double progressStep = 100.0 / (points.size() - 1);

        int i = 0;
        for (int u = 0; u < points.size() && !canceled; u++) {
            for (int v = (u + 1); v < points.size() && !canceled; v++) {
                final long edge = graph.getEdge(u, v);
                edges[i] = edge;
                final Path path = solver.calculateShortestPath(points.get(u), points.get(v));
                if (path == null) {
                    canceled = true;
                } else {
                    weights[i] = path.getLength();
                    mapping.put(edge, path);
                }
                ++i;
            }
            fireProgressDone(progressStep);
        }
        return !canceled ? new Graph(points.size(), edges, weights) : null;
    }

    @Override
    public void cancelCalculation() {
        canceled = true;
        solver.cancelCalculation();
    }

    @Override
    protected ISPSPSolver createSPSPSolver() {
        return new ReusableDijkstra(graph);
    }

}