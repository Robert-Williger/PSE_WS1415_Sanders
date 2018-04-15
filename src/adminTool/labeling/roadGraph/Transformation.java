package adminTool.labeling.roadGraph;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Shape;
import java.util.HashMap;
import java.util.List;

import adminTool.IPointAccess;
import adminTool.Util;
import adminTool.elements.MultiElement;
import util.IntList;

public class Transformation {

    private final int wayWidth;
    private final int threshold;

    public Transformation(final int wayWidth, final int threshold) {
        this.wayWidth = wayWidth;
        this.threshold = threshold;
    }

    public void transform(final IPointAccess points, final List<? extends MultiElement> paths) {
        HashMap<Integer, IntList> map = new HashMap<>();
        for (int i = 0; i < paths.size(); ++i) {
            final MultiElement element = paths.get(i);
            append(map, element.getNode(0), i);
            append(map, element.getNode(element.size() - 1), i);
        }

        for (final HashMap.Entry<Integer, IntList> entry : map.entrySet()) {
            final int node = entry.getKey();
            final IntList list = entry.getValue();
        }
    }

    private void append(final HashMap<Integer, IntList> map, final int node, final int i) {
        IntList list = map.get(node);
        if (list == null) {
            list = new IntList();
            map.put(node, list);
        }
        list.add(i);
    }

    private Shape formShape(final IPointAccess points, final MultiElement element) {
        return Util.createStrokedShape(points, element,
                new BasicStroke(wayWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
    }

    private MultiElement subElement(final IPointAccess points, final MultiElement element) {
        double totalLength = 0;

        int lastX = points.getX(element.getNode(0));
        int lastY = points.getY(element.getNode(0));
        for (int i = 1; i < element.size(); i++) {
            int currentX = points.getX(element.getNode(i));
            int currentY = points.getY(element.getNode(i));
            totalLength += Point.distance(currentX, currentY, lastX, lastY);
            if (totalLength >= threshold) {
                return element.subElement(0, i + 1);
            }
            lastX = currentX;
            lastY = currentY;
        }
        return element;
    }
}
