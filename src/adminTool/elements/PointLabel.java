package adminTool.elements;

public class PointLabel extends POI {

    final String name;

    public PointLabel(int index, int type, String name) {
        super(index, type);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
