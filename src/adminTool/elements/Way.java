package adminTool.elements;

public class Way extends MultiElement {

    private final String name;
    private final boolean oneway;

    public Way(final int[] indices, final int type, final String name, final boolean oneway) {
        super(indices, type);

        this.oneway = oneway;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isOneway() {
        return oneway;
    }
}