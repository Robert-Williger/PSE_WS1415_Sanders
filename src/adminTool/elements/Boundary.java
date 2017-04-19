package adminTool.elements;

import adminTool.elements.Node;

public class Boundary {

    private final String name;
    private final Node[][] inner;
    private final Node[][] outer;

    public Boundary(final String name, final Node[][] outer, final Node[][] inner) {
        this.name = name;
        this.inner = inner;
        this.outer = outer;
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
}
