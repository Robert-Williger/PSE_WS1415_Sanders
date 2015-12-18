package model.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import model.IProgressListener;

public class ChristofidesTSPSolver extends AbstractRouteSolver implements IRouteSolver {

    private boolean canceled;
    private ISPSPSolver solver;
    private Path[] completeMapping;
    private List<Integer> matchingMapping;

    public ChristofidesTSPSolver(final IDirectedGraph undirectedGraph) {
        super(undirectedGraph);

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
    public Route calculateRoute(final List<InterNode> edges) {
        fireProgressDone(-1);

        canceled = false;

        final IUndirectedGraph completeGraph = createCompleteGraph(edges);

        if (completeGraph != null) {
            final IUndirectedGraph mstGraph = new JPMST(completeGraph).calculateMST();

            final IUndirectedGraph matchingGraph = createMatchingGraph(mstGraph, completeGraph);

            final Collection<Long> matchingEdges = new BlossomAlgorithm().calculatePerfectMatching(matchingGraph);

            final IUndirectedGraph eulerianGraph = createEulerianGraph(matchingGraph, mstGraph, matchingEdges);

            return createTSPRoute(eulerianGraph);
        }

        return emptyRoute();
    }

    @Override
    protected ISPSPSolver createSPSPSolver() {
        return new ReusableDijkstra(graph);
    }

    private IUndirectedGraph createCompleteGraph(final List<InterNode> points) {
        final int size = points.size();
        completeMapping = new Path[size * (size - 1) / 2];
        final int[] weights = new int[completeMapping.length];
        final int[] firstNodes = new int[completeMapping.length];
        final int[] secondNodes = new int[completeMapping.length];

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

    private IUndirectedGraph createMatchingGraph(final IUndirectedGraph mstGraph, final IUndirectedGraph completeGraph) {
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
        final int[] firstNodes = new int[size * (size - 1) / 2];
        final int[] secondNodes = new int[firstNodes.length];
        final int[] weights = new int[firstNodes.length];

        int count = 0;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                firstNodes[count] = i;
                secondNodes[count] = j;
                weights[count] = completeGraph.getWeight(completeGraph.getEdge(matchingMapping.get(i),
                        matchingMapping.get(j)));
                ++count;
            }
        }

        return new UndirectedGraph(matchingMapping.size(), firstNodes, secondNodes, weights);
    }

    private IUndirectedGraph createEulerianGraph(final IUndirectedGraph matchingGraph, final IUndirectedGraph mstGraph,
            final Collection<Long> matchingEdges) {

        final int[] firstNodes = new int[mstGraph.getEdges() + matchingEdges.size()];
        final int[] secondNodes = new int[firstNodes.length];
        final int[] weights = new int[firstNodes.length];

        int i = 0;

        for (final long edge : matchingEdges) {
            firstNodes[i] = matchingMapping.get(matchingGraph.getFirstNode(edge));
            secondNodes[i] = matchingMapping.get(matchingGraph.getSecondNode(edge));
            ++i;
        }

        for (int node = 0; node < mstGraph.getNodes(); node++) {
            for (final Iterator<Integer> it = mstGraph.getAdjacentNodes(node); it.hasNext();) {
                final int otherNode = it.next();
                if (otherNode < node) {
                    firstNodes[i] = node;
                    secondNodes[i] = otherNode;
                    ++i;
                }
            }
        }

        return new UndirectedGraph(mstGraph.getNodes(), firstNodes, secondNodes, weights);
    }

    private Route createTSPRoute(final IUndirectedGraph eulerianGraph) {
        final int nodes = eulerianGraph.getNodes();

        int currentPath = 0;
        final Path[][] paths = new Path[2][nodes];
        final int[][] targetIndices = new int[2][nodes];

        int minRouteLength = Integer.MAX_VALUE;

        final EulerianCircuitAlgorithm algorithm = new EulerianCircuitAlgorithm();

        // TODO only do once?

        for (int startNode = 0; startNode < nodes; startNode++) {
            final List<Integer> eulerianPath = algorithm.getEulerianCurcuit(eulerianGraph, startNode);

            final Path[] currentRoute = paths[currentPath];
            final int[] currentIndices = targetIndices[currentPath];
            // final List<Path> currentRoute = new ArrayList<Path>();
            int currentRouteLength = 0;

            final Set<Integer> usedNodes = new HashSet<Integer>();

            final Iterator<Integer> iterator = eulerianPath.iterator();
            int last = iterator.next();

            usedNodes.add(last);
            int count = 0;
            while (iterator.hasNext()) {
                int current = iterator.next();
                if (usedNodes.add(current)) {
                    final Path path = completeMapping[getPathIndex(last, current, nodes)];
                    currentIndices[last] = count;
                    currentRoute[count] = path;
                    ++count;
                    currentRouteLength += path.getLength();
                    last = current;
                }
            }
            final Path path = completeMapping[getPathIndex(last, eulerianPath.get(0), nodes)];
            currentIndices[last] = count;
            currentRoute[count] = path;
            currentRouteLength += path.getLength();

            if (currentRouteLength < minRouteLength) {
                minRouteLength = currentRouteLength;
                currentPath = 1 - currentPath;
            }
        }

        return new Route(paths[currentPath], targetIndices[currentPath]);
    }
}
