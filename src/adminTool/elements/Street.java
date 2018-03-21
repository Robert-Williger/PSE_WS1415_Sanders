package adminTool.elements;

public class Street extends Way {

    private final int id;

    public Street(final int[] indices, final int type, final String name, final int id) {
        super(indices, type, name);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}