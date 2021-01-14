package adminTool.addressIndex;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PrimitiveIterator;

import adminTool.elements.Boundary;
import adminTool.elements.IPointAccess;
import adminTool.elements.Street;
import adminTool.quadtree.IQuadtree;
import adminTool.quadtree.Quadtree;
import adminTool.quadtree.policies.BoundaryQuadtreePolicy;

public class StreetLocator {
    private static final int DEFAULT_MAX_ELEMENTS_PER_TILE = 8;
    private static final int DEFAULT_MAX_HEIGHT = 20;

    private double size;
    private final IPointAccess points;

    private List<IQuadtree> quadtrees;
    private List<BoundaryQuadtreePolicy> policies;
    private List<List<Boundary>> boundaries;

    public StreetLocator(final Collection<Boundary> boundaries, final IPointAccess points,
            final Rectangle2D mapBounds) {
        this.points = points;

        this.size = Math.max(mapBounds.getMaxX(), mapBounds.getMaxY());
        createQuadtrees(boundaries, points);
    }

    private void createQuadtrees(final Collection<Boundary> boundaries, final IPointAccess points) {
        int maxType = 0;
        for (final Boundary boundary : boundaries) {
            maxType = Math.max(maxType, boundary.getType());
        }
        quadtrees = new ArrayList<>(maxType + 1);
        policies = new ArrayList<>(maxType + 1);
        this.boundaries = new ArrayList<>(maxType + 1);
        for (int i = 0; i <= maxType; ++i) {
            this.boundaries.add(new ArrayList<>());
        }
        for (final Boundary boundary : boundaries) {
            this.boundaries.get(boundary.getType()).add(boundary);
        }
        for (final List<Boundary> b : this.boundaries) {
            final BoundaryQuadtreePolicy policy = new BoundaryQuadtreePolicy(b, points);
            final IQuadtree quadtree = new Quadtree(b.size(), policy, size, DEFAULT_MAX_HEIGHT,
                    DEFAULT_MAX_ELEMENTS_PER_TILE);
            policies.add(policy);
            quadtrees.add(quadtree);
        }
    }

    public String getCity(final Street street) {
        for (int i = quadtrees.size() - 1; i >= 0; --i) {
            final IQuadtree quadtree = quadtrees.get(i);
            final BoundaryQuadtreePolicy policy = policies.get(i);
            final List<Boundary> boundaries = this.boundaries.get(i);
            final int node = street.getPoint(street.size() / 2);

            IQuadtree loc = quadtree.locate(0, 0, size, points.getX(node), points.getY(node));
            for (PrimitiveIterator.OfInt it = loc.getElements().iterator(); it.hasNext();) {
                int element = it.nextInt();
                if (policy.contains(element, points.getX(node), points.getY(node))) {
                    final Boundary boundary = boundaries.get(element);
                    return boundary.getName();
                }
            }
        }

        return null;
    }
}
