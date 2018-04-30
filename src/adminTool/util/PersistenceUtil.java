package adminTool.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adminTool.IPointAccess;
import adminTool.UnboundedPointAccess;
import adminTool.elements.MultiElement;

public final class PersistenceUtil {

    public static void write(final DataOutputStream output, final IPointAccess points) throws IOException {
        output.writeInt(points.getPoints());
        for (int i = 0; i < points.getPoints(); ++i) {
            output.writeInt(points.getX(i));
            output.writeInt(points.getY(i));
        }
    }

    public static void write(final DataOutputStream output, final List<? extends MultiElement> elements)
            throws IOException {
        output.writeInt(elements.size());
        for (final MultiElement element : elements) {
            write(output, element);
        }
    }

    public static void write(final DataOutputStream output, final MultiElement element) throws IOException {
        output.writeInt(element.size());
        output.writeInt(element.getType());
        for (int i = 0; i < element.size(); ++i) {
            output.writeInt(element.getNode(i));
        }
    }

    public static UnboundedPointAccess readPoints(final DataInputStream input) throws IOException {
        final int size = input.readInt();
        final UnboundedPointAccess points = new UnboundedPointAccess(size);
        for (int i = 0; i < size; ++i) {
            points.addPoint(input.readInt(), input.readInt());
        }
        return points;
    }

    public static List<MultiElement> readElements(final DataInputStream input) throws IOException {
        final int size = input.readInt();
        final List<MultiElement> elements = new ArrayList<MultiElement>(size);
        for (int i = 0; i < size; ++i) {
            elements.add(readElement(input));
        }
        return elements;
    }

    public static MultiElement readElement(final DataInputStream input) throws IOException {
        final int[] indices = new int[input.readInt()];
        final int type = input.readInt();
        for (int i = 0; i < indices.length; ++i) {
            indices[i] = input.readInt();
        }
        return new MultiElement(indices, type);
    }
}
