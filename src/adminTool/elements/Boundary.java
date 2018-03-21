package adminTool.elements;

public class Boundary {

    private static int count;
    private final int id;
    private final String name;
    private final int[][] inner;
    private final int[][] outer;

    public Boundary(final String name, final int[][] outer, final int[][] inner) {
        this.name = name;
        this.inner = inner;
        this.outer = outer;
        id = count++;
    }

    public int[][] getOuter() {
        return outer;
    }

    public int[][] getInner() {
        return inner;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }
}
