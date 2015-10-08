package model.routing;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import model.AbstractModel;
import model.IProgressListener;
import model.elements.StreetNode;
import model.map.IMapManager;
import model.renderEngine.IRenderRoute;
import model.renderEngine.RenderRoute;
import model.targets.IPointList;
import model.targets.IRoutePoint;
import model.targets.PointList;
import model.targets.RoutePoint;

public class RouteManager extends AbstractModel implements IRouteManager {

    private IGraph graph;
    private final IMapManager manager;
    private final IPointList pointList;

    private final IComplexRouteSolver[] routeSolvers;
    private final String[] routeSolverNames;

    private int routeSolver;
    private boolean calculating;

    public RouteManager(final IGraph graph, final IMapManager manager) {
        this.graph = graph;
        this.manager = manager;

        this.routeSolverNames = createNames();
        this.routeSolvers = createRouteSolvers(graph);

        setRouteSolver(0);

        pointList = new PointList();
    }

    private IComplexRouteSolver getCurrentRouteSolver() {
        return routeSolvers[routeSolver];
    }

    private List<InterNode> createInterNodeList() {
        final List<InterNode> interNodeList = new ArrayList<InterNode>();

        for (int i = 0; i < pointList.getSize(); i++) {
            final StreetNode streetNode = pointList.get(i).getStreetNode();
            final InterNode interNode = new InterNode(streetNode.getStreet().getID(), streetNode.getOffset());
            interNodeList.add(interNode);
        }
        return interNodeList;
    }

    private float[] calcStreetPartInterval(final InterNode node, final long edge) {
        final float[] ret = new float[2];

        final int firstNode = graph.getFirstNode(edge);
        final int secondNode = graph.getSecondNode(edge);
        if (graph.getFirstNode(node.getEdge()) == firstNode || graph.getFirstNode(node.getEdge()) == secondNode) {
            ret[0] = 0;
            ret[1] = node.getOffset();
        } else {
            ret[0] = node.getOffset();
            ret[1] = 1;
        }
        return ret;
    }

    private Rectangle calcBounds() {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (final IRoutePoint routePoint : pointList) {
            final int x = routePoint.getStreetNode().getLocation().x;
            final int y = routePoint.getStreetNode().getLocation().y;
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);

            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    protected IRenderRoute createRenderRoute(final List<Path> paths) {
        int length = 0;

        for (final Path p : paths) {
            if (p != null) {
                length += p.getLength();
            } else {
                return null;
            }

        }
        final RenderRoute renderRoute = new RenderRoute(length, calcBounds(), pointList);

        float[] interval;

        for (final Path p : paths) {
            final List<Long> edges = p.getEdges();

            if (edges.size() == 0) {
                final InterNode start = p.getStartNode();
                final InterNode end = p.getEndNode();
                if (start.getEdge() == end.getEdge()) {
                    renderRoute.addStreetPart(start.getEdge(), start.getOffset(), end.getOffset());
                } else {
                    final int[] nodes0 = {graph.getFirstNode(start.getEdge()), graph.getSecondNode(start.getEdge())};
                    final int[] nodes1 = {graph.getFirstNode(end.getEdge()), graph.getSecondNode(end.getEdge())};

                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) {
                            if (nodes0[i] == nodes1[j]) {
                                if (i == 0) {
                                    renderRoute.addStreetPart(start.getEdge(), 0, start.getOffset());
                                } else {
                                    renderRoute.addStreetPart(start.getEdge(), start.getOffset(), 1);
                                }
                                if (j == 0) {
                                    renderRoute.addStreetPart(end.getEdge(), 0, end.getOffset());
                                } else {
                                    renderRoute.addStreetPart(end.getEdge(), end.getOffset(), 1);
                                }
                            }
                        }
                    }
                }
            } else {
                interval = calcStreetPartInterval(p.getStartNode(), edges.get(0));
                renderRoute.addStreetPart(p.getStartNode().getEdge(), interval[0], interval[1]);

                for (final long e : edges) {
                    renderRoute.addStreet(e);
                }

                interval = calcStreetPartInterval(p.getEndNode(), edges.get(edges.size() - 1));
                renderRoute.addStreetPart(p.getEndNode().getEdge(), interval[0], interval[1]);
            }
        }

        return renderRoute;
    }

    protected IComplexRouteSolver[] createRouteSolvers(final IGraph graph) {
        return new IComplexRouteSolver[]{new ViaRouteSolver(graph), new ChristofidesTSPSolver(graph)};
    }

    protected String[] createNames() {
        return new String[]{"Via-Route", "TSP-Route"};
    }

    @Override
    public void addProgressListener(final IProgressListener listener) {
        for (final IComplexRouteSolver solver : routeSolvers) {
            solver.addProgressListener(listener);
        }
    }

    @Override
    public void removeProgressListener(final IProgressListener listener) {
        for (final IComplexRouteSolver solver : routeSolvers) {
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