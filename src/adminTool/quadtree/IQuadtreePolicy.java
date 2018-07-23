package adminTool.quadtree;

@FunctionalInterface
public interface IQuadtreePolicy {

    boolean intersects(final int element, final int height, final double x, final double y, final double size);

}
