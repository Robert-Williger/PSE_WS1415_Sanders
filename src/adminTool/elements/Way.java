package adminTool.elements;

import util.IntList;

public class Way extends MultiElement {

    private final String name;
    private final boolean oneway;

    public Way(final MultiElement element, final int type, final String name, final boolean oneway) {
        super(element, type);

        this.oneway = oneway;
        this.name = name;
    }

    public Way(final IntList indices, final int type, final String name, final boolean oneway) {
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