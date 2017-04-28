package model.routing;

import java.util.List;
import java.util.PrimitiveIterator.OfInt;

import util.AddressableBinaryHeap;
import util.IAddressablePriorityQueue;
import util.IntList;
import model.IProgressListener;

public class MSTTSPSolver extends AbstractRouteSolver {
    private IUndirectedGraph mst;
    private Path[] completeMapping;
    private IntList tspNodes;
    private boolean canceled;
    private final ISPSPSolver solver;

    public MSTTSPSolver(final IDirectedGraph graph) {
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
        return new AddressableBinaryHeap<>();
    }

    @Override
    public Route calculateRoute(final List<InterNode> edges) {
        fireProgressDone(-1);

        canceled = false;

        final IUndirectedGraph completeGraph = createCompleteGraph(edges);

        if (!canceled) {
            mst = new JPMST(completeGraph).calculateMST();
            final int num = mst.getNodes();

            IntList minRoute = null;
            int minRouteLength = Integer.MAX_VALUE;

            for (int i = 0; i < completeGraph.getNodes(); i++) {
                tspNodes = new IntList();
                tspNodes.add(i);
                dfs(i, i);

                int weightSum = 0;
                for (int j = 0; j < num; j++) {
                    weightSum += completeMapping[getPathIndex(tspNodes.get(j), tspNodes.get((j + 1) % num),
                            edges.size())].getLength();
                }

                if (weightSum < minRouteLength) {
                    minRoute = tspNodes;
                    minRouteLength = weightSum;
                }
            }

            final Path[] paths = new Path[num];
            final int[] targetIndices = new int[num];
            for (int j = 0; j < num; j++) {
                paths[j] = completeMapping[getPathIndex(minRoute.get(j), minRoute.get((j + 1) % num), edges.size())];
                targetIndices[minRoute.get(j)] = j;
            }

            return new Route(paths, targetIndices);
        }

        return emptyRoute();
    }

    private void dfs(final int u, final int v) {

        final OfInt it = mst.getAdjacentNodes(v);
        while (it.hasNext()) {
            final int w = it.nextInt();
            if (w != u) {
                tspNodes.add(w);
                dfs(v, w);
            }
        }
    }

    private IUndirectedGraph createCompleteGraph(final List<InterNode> points) {
        final int size = points.size();
        completeMapping = new Path[size * (size - 1) / 2];
        final int[] weights = new int[completeMapping.length];
        final int[] firstNodes = new int[completeMapping.length];
        final int[] secondNodes = new int[firstNodes.length];

        final double progressStep = 100.0 / (points.size() - 1);

        for (int i = 0; i < weights.length; i++) {
            weights[i] = Integer.MAX_VALUE;
        }
        for (int u = 0; u < points.size() && !canceled; u++) {
            for (int v = u + 1; v < points.size() && !canceled; v++) {
                final Path path = solver.calculateShortestPath(points.get(u), points.get(v));
                // TODO can we handle this?
                if (path == null) {
                    canceled = true;
                    return null;
                }
                final int index = getPathIndex(u, v, size);
                if (path.getLength() < weights[index]) {
                    firstNodes[index] = u;
                    secondNodes[index] = v;
                    weights[index] = path.getLength();
                    completeMapping[index] = path;
                }
            }
            fireProgressDone(progressStep);
        }
        return new UndirectedGraph(size, firstNodes, secondNodes, weights);
    }

    private int getPathIndex(final int firstNode, final int secondNode, final int nodes) {
        int min = Math.min(firstNode, secondNode);
        int max = Math.max(firstNode, secondNode);
        return (nodes * (nodes - 1) - (nodes - min) * (nodes - min - 1)) / 2 + max - min - 1;
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