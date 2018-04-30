package adminTool.quadtree;

public interface IQuadtreePolicy {

    boolean intersects(final int element, final int height, final int x, final int y, final int size);

}
