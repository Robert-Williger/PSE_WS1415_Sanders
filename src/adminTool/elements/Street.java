package adminTool.elements;

import util.IntList;

public class Street extends Way {

    private final int id;

    public Street(final IntList indices, final int type, final String name, final int id) {
        super(indices, type, name, false); // TODO fix this
        this.id = id;
    }

    public int getId() {
        return id;
    }
}