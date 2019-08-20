package adminTool.elements;

import util.IntList;

public class LineLabel extends MultiElement {

    final String name;
    final int zoom;

    public LineLabel(final IntList indices, final int type, final String name, final int zoom) {
        super(indices, type);
        this.name = name;
        this.zoom = zoom;
    }

    public LineLabel(final MultiElement element, final int type, final String name, final int zoom) {
        super(element, type);
        this.name = name;
        this.zoom = zoom;
    }

    public String getName() {
        return name;
    }

    public int getZoom() {
        return zoom;
    }
}
