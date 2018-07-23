package adminTool.labeling.roadGraph;

import adminTool.IElement;
import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;

public class ElementAdapter implements IElement {
    private final IPointAccess points;
    private MultiElement element;

    public ElementAdapter(final IPointAccess points) {
        this.points = points;
    }

    public void setMultiElement(final MultiElement element) {
        this.element = element;
    }

    @Override
    public int size() {
        return element.size();
    }

    @Override
    public double getX(final int index) {
        return points.getX(element.getNode(index));
    }

    @Override
    public double getY(final int index) {
        return points.getY(element.getNode(index));
    }

}
