package adminTool.quadtree;

@FunctionalInterface
public interface ICollisionPolicy {

    boolean intersect(int element1, int element2, int height);

}
