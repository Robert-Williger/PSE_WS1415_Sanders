package model.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import model.IProgressListener;

public class ChristofidesTSPSolver extends AbstractComplexRouteSolver implements IComplexRouteSolver {

    private boolean canceled;
    private ISPSPSolver solver;
    private HashMap<Long, Path> completeMapping;
    private List<Integer> matchingMapping;

    public ChristofidesTSPSolver(final IGraph graph) {
        super(graph);

        this.solver = createSPSPSolver();
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

    @Override
    public void cancelCalculation() {
        canceled = true;
        solver.cancelCalculation();
    }

    @Override
    public List<Path> calculateRoute(final List<InterNode> edges) {
        fireProgressDone(-1);

        completeMapping = new HashMap<Long, Path>();
        canceled = false;

        final IGraph completeGraph = createCompleteGraph(edges);

        if (completeGraph != null) {
            final IGraph mstGraph = new JPMST(completeGraph).calculateMST();

            final IGraph matchingGraph = createMatchingGraph(mstGraph, completeGraph);

            final Collection<Long> matchingEdges = new BlossomAlgorithm().calculatePerfectMatching(matchingGraph);

            final IGraph eulerianGraph = createEulerianGraph(matchingGraph, mstGraph, matchingEdges);

            return createTSPRoute(eulerianGraph);
        }

        return new ArrayList<Path>();
    }

    @Override
    protected ISPSPSolver createSPSPSolver() {
        return new ReusableDijkstra(graph);
    }

    private IGraph createCompleteGraph(final List<InterNode> points) {
        final int size = points.size();
        final long[] edges = new long[size * (size - 1) / 2];
        final int[] weights = new int[edges.length];

        final double progressStep = 100.0 / (points.size() - 1);

        int i = 0;
        for (int u = 0; u < points.size() && !canceled; u++) {
            for (int v = u + 1; v < points.size() && !canceled; v++) {
                final long edge = graph.getEdge(u, v);
                edges[i] = edge;
                final Path path = solver.calculateShortestPath(points.get(u), points.get(v));
                if (path == null) {
                    canceled = true;
                    return null;
                }
                weights[i] = path.getLength();
                completeMapping.put(edge, path);
                ++i;
            }
            fireProgressDone(progressStep);
        }
        return new Graph(size, edges, weights);
    }

    private IGraph createMatchingGraph(final IGraph mstGraph, final IGraph completeGraph) {
        matchingMapping = new ArrayList<Integer>();

        for (int node = 0; node < mstGraph.getNodes(); node++) {
            int count = 0;
            for (final Iterator<Integer> it = mstGraph.getAdjacentNodes(node); it.hasNext(); it.next()) {
                ++count;
            }
            if (count % 2 == 1) {
                matchingMapping.add(node);
            }
        }

        final int size = matchingMapping.size();
        final long[] edges = new long[size * (size - 1) / 2];// <Long>();
        final int[] weights = new int[edges.length];

        int count = 0;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                edges[count] = completeGraph.getEdge(i, j);
                weights[count] = completeGraph.getWeight(completeGraph.getEdge(matchingMapping.get(i),
                        matchingMapping.get(j)));
                ++count;
            }
        }

        return new Graph(matchingMapping.size(), edges, weights);
    }

    private IGraph createEulerianGraph(final IGraph matchingGraph, final IGraph mstGraph,
            final Collection<Long> matchingEdges) {
        final long[] eulerianEdges = new long[mstGraph.getEdges() + matchingEdges.size()];
        final int[] eulerianWeights = new int[mstGraph.getEdges() + matchingEdges.size()];

        int i = 0;
        for (final long edge : matchingEdges) {
            eulerianEdges[i] = graph.getEdge(matchingMapping.get(matchingGraph.getFirstNode(edge)),
                    matchingMapping.get(matchingGraph.getSecondNode(edge)));
            eulerianWeights[i] = 0;
            ++i;
        }

        for (int node = 0; node < mstGraph.getNodes(); node++) {
            for (final Iterator<Integer> it = mstGraph.getAdjacentNodes(node); it.hasNext();) {
                final int otherNode = it.next();
                if (otherNode < node) {
                    eulerianEdges[i] = mstGraph.getEdge(node, otherNode);
                    eulerianWeights[i] = 0;
                    ++i;
                }
            }
        }

        return new Graph(mstGraph.getNodes(), eulerianEdges, eulerianWeights);
    }

    private List<Path> createTSPRoute(final IGraph eulerianGraph) {
        List<Path> tspRoute = new ArrayList<Path>();
        int minRouteLength = Integer.MAX_VALUE;

        final EulerianCircuitAlgorithm algorithm = new EulerianCircuitAlgorithm();

        // TODO only do once?
        for (int startNode = 0; startNode < eulerianGraph.getNodes(); startNode++) {
            final List<Integer> eulerianPath = algorithm.getEulerianCurcuit(eulerianGraph, startNode);

            final List<Path> currentRoute = new ArrayList<Path>();
            int currentRouteLength = 0;

            final Set<Integer> usedNodes = new HashSet<Integer>();

            final Iterator<Integer> iterator = eulerianPath.iterator();
            int last = iterator.next();

            usedNodes.add(last);
            while (iterator.hasNext()) {
                int current = iterator.next();
                if (usedNodes.add(current)) {
                    final long edge = graph.getEdge(last, current);
                    currentRoute.add(completeMapping.get(edge));
                    currentRouteLength += graph.getWeight(edge);
                    last = current;
                }
            }
            final long edge = graph.getEdge(last, eulerianPath.get(0));
            currentRoute.add(completeMapping.get(edge));
            currentRouteLength += graph.getWeight(edge);

            if (currentRouteLength < minRouteLength) {
                minRouteLength = currentRouteLength;
                tspRoute = currentRoute;
            }
        }

        return tspRoute;
    }
}
