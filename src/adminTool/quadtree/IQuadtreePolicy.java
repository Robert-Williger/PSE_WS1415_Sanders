package adminTool.quadtree;

public interface IQuadtreePolicy {

    boolean intersects(final int element, final int zoom, final int x, final int y, final int size);

    int getMaxElementsPerTile();

    int getMaxZoomSteps();

}
