package adminTool.labeling;

import adminTool.elements.MultiElement;
import util.IntList;

public class Label extends MultiElement {

    final String name;

    public Label(final IntList indices, final int type, final String name) {
        super(indices, type);
        this.name = name;
    }

    public Label(final MultiElement element, final int type, final String name) {
        super(element, type);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
