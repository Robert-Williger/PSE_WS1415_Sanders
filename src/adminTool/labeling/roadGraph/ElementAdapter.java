package adminTool.labeling.roadGraph;

import java.awt.Point;

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

    public double getLength() {
        double totalLength = 0;

        double lastX = getX(0);
        double lastY = getY(0);
        for (int i = 1; i < size(); i++) {
            double currentX = getX(i);
            double currentY = getY(i);
            totalLength += Point.distance(currentX, currentY, lastX, lastY);
            lastX = currentX;
            lastY = currentY;
        }

        return totalLength;
    }

}
