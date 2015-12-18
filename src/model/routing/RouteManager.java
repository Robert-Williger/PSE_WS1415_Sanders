package model.routing;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import model.AbstractModel;
import model.IProgressListener;
import model.elements.IStreet;
import model.elements.StreetNode;
import model.map.IMapManager;
import model.renderEngine.IRenderRoute;
import model.renderEngine.RenderRoute;
import model.targets.IPointList;
import model.targets.IRoutePoint;
import model.targets.PointList;
import model.targets.RoutePoint;

public class RouteManager extends AbstractModel implements IRouteManager {

    private final IDirectedGraph graph;
    private final IMapManager manager;
    private final IPointList pointList;

    private final IRouteSolver[] routeSolvers;
    private final String[] routeSolverNames;

    private int routeSolver;
    private boolean calculating;

    public RouteManager(final IDirectedGraph graph, final IMapManager manager) {
        this.graph = graph;
        this.manager = manager;

        this.routeSolverNames = createNames();
        this.routeSolvers = createRouteSolvers(graph);

        setRouteSolver(0);

        pointList = new PointList();
    }

    private IRouteSolver getCurrentRouteSolver() {
        return routeSolvers[routeSolver];
    }

    private List<InterNode> createInterNodeList() {
        final List<InterNode> interNodeList = new ArrayList<InterNode>(pointList.size());

        for (int i = 0; i < pointList.size(); i++) {
            final StreetNode streetNode = pointList.get(i).getStreetNode();
            final IStreet street = streetNode.getStreet();

            // TODO own instance for mapping from id to graph edges?
            int edge = street.getID();
            int correspondingEdge = !street.isOneWay() ? (street.getID() | 0x80000000) : -1;
            final InterNode interNode = new InterNode(edge, correspondingEdge, streetNode.getOffset());
            interNodeList.add(interNode);
        }
        return interNodeList;
    }

    private Rectangle calcBounds() {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (final IRoutePoint routePoint : pointList) {
            final int x = routePoint.getStreetNode().getX();
            final int y = routePoint.getStreetNode().getY();
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);

            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    protected IRenderRoute createRenderRoute(final Route route) {
        int length = 0;

        for (final Path p : route.getPaths()) {
            if (p != null) {
                length += p.getLength();
            } else {
                return null;
            }

        }
        final RenderRoute renderRoute = new RenderRoute(length, calcBounds());

        float[] interval;

        int index = 0;
        for (final IRoutePoint point : pointList) {
            point.setTargetIndex(route.getTargetIndex(index++));
        }

        for (final Path p : route.getPaths()) {
            final List<Integer> edges = p.getEdges();

            if (!edges.isEmpty()) {
                interval = calcStreetPartInterval(p.getStartNode(), graph.getStartNode(edges.get(0)));
                renderRoute.addStreetPart(p.getStartNode().getEdge(), interval[0], interval[1]);

                for (final int e : edges) {
                    renderRoute.addStreet(e);
                }

                interval = calcStreetPartInterval(p.getEndNode(), graph.getEndNode(edges.get(edges.size() - 1)));
                renderRoute.addStreetPart(p.getEndNode().getEdge(), interval[0], interval[1]);
            } else {
                final InterNode start = p.getStartNode();
                final InterNode end = p.getEndNode();
                if (start.getEdge() == end.getEdge()) {
                    renderRoute.addStreetPart(start.getEdge(), start.getOffset(), end.getOffset());
                } else {
                    final int[] startNodes = {graph.getStartNode(start.getEdge()), graph.getEndNode(start.getEdge())};
                    final int[] endNodes = {graph.getStartNode(end.getEdge()), graph.getEndNode(end.getEdge())};

                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) {
                            if (startNodes[i] == endNodes[j]) {
                                renderRoute.addStreetPart(start.getEdge(), Math.min(i, start.getOffset()),
                                        Math.max(i, start.getOffset()));

                                renderRoute.addStreetPart(end.getEdge(), Math.min(j, end.getOffset()),
                                        Math.max(j, end.getOffset()));

                            }
                        }
                    }
                }
            }
        }

        return renderRoute;
    }

    private float[] calcStreetPartInterval(final InterNode routePoint, final int graphNode) {
        final float[] ret = new float[2];

        if (graph.getEndNode(routePoint.getEdge()) == graphNode) {
            ret[0] = routePoint.getOffset();
            ret[1] = 1;
        } else {
            ret[0] = 0;
            ret[1] = routePoint.getOffset();
        }

        return ret;
    }

    protected IRouteSolver[] createRouteSolvers(final IDirectedGraph graph) {
        return new IRouteSolver[]{new ViaRouteSolver(graph), new ChristofidesTSPSolver(graph), new MSTTSPSolver(graph),
                new BruteForceTSP(graph)};
    }

    protected String[] createNames() {
        return new String[]{"Via-Route", "TSP-Route (1.5 Approximation)", "TSP-Route (2.0 Approxation)",
                "TSP-Route (Brute-Force)"};
    }

    @Override
    public void addProgressListener(final IProgressListener listener) {
        for (final IRouteSolver solver : routeSolvers) {
            solver.addProgressListener(listener);
        }
    }

    @Override
    public void removeProgressListener(final IProgressListener listener) {
        for (final IRouteSolver solver : routeSolvers) {
            solver.removeProgressListener(listener);
        }
    }

    @Override
    public void cancelCalculation() {
        getCurrentRouteSolver().cancelCalculation();
    }

    @Override
    public IRenderRoute calculateRoute() {
        calculating = true;
        fireChange();

        final IRenderRoute route = createRenderRoute(getCurrentRouteSolver().calculateRoute(createInterNodeList()));
        calculating = false;

        fireChange();

        return route;
    }

    @Override
    public IRoutePoint createPoint() {
        return new RoutePoint(manager);
    }

    @Override
    public IPointList getPointList() {
        return pointList;
    }

    @Override
    public String[] getRouteSolvers() {
        return routeSolverNames;
    }

    @Override
    public void setRouteSolver(final int solver) {
        routeSolver = solver;
    }

    @Override
    public int getRouteSolver() {
        return routeSolver;
    }

    @Override
    public boolean isCalculating() {
        return calculating;
    }

}