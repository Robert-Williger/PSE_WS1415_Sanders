package adminTool.elements;

public class Way extends MultiElement {

    private final String name;

    public Way(final int[] indices, final int type, final String name) {
        super(indices, type);

        this.name = name;
    }

    public String getName() {
        return name;
    }
}