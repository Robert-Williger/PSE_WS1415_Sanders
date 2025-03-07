package adminTool.elements;

public class POI implements Typeable {

    private int index;
    private final int type;

    public POI(final int index, final int type) {
        this.index = index;
        this.type = type;
    }

    @Override
    public int getType() {
        return type;
    }

    public int getPoint() {
        return index;
    }

    public void setNode(final int index) {
        this.index = index;
    }
}