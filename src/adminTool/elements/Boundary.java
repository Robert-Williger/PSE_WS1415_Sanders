package adminTool.elements;

import adminTool.elements.Node;

public class Boundary {

    private static int count;
    private final int id;
    private final String name;
    private final Node[][] inner;
    private final Node[][] outer;

    public Boundary(final String name, final Node[][] outer, final Node[][] inner) {
        this.name = name;
        this.inner = inner;
        this.outer = outer;
        id = count++;
    }

    public Node[][] getOuter() {
        return outer;
    }

    public Node[][] getInner() {
        return inner;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }
}
