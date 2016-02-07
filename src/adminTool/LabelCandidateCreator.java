package adminTool;

import java.awt.Font;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.Iterator;

import model.map.IPixelConverter;
import adminTool.elements.IWay;
import adminTool.elements.LabelCandidate;
import adminTool.elements.Node;

public class LabelCandidateCreator {

    private IPixelConverter converter;

    public LabelCandidateCreator(final IPixelConverter converter) {
        this.converter = converter;
    }

    // first fit
    public LabelCandidate createFirstClassCandidates(final IWay way, final int zoom) {
        final String name = way.getName();
        if (name.isEmpty()) {
            return null;
        }
        final TextLayout layout = new TextLayout(name, new Font("Arial", Font.BOLD, 20), new FontRenderContext(
                new AffineTransform(), true, false));
        final int textPixelLength = (int) layout.getBounds().getWidth();
        final int textCoordLength = converter.getCoordDistance(textPixelLength, zoom);

        Iterator<Node> iterator = way.iterator();
        Node last = iterator.next();

        int count = 0;
        while (iterator.hasNext()) {
            Node current = iterator.next();

            int distance = (int) Point.distance(last.getX(), last.getY(), current.getX(), current.getY());
            if (distance > textCoordLength) {
                ++count;
            }

            last = current;
        }

        iterator = way.iterator();
        last = iterator.next();

        if (count > 0) {
            final int[] points = new int[count << 1];
            final float[] angles = new float[count];
            count = 0;
            while (iterator.hasNext()) {
                Node current = iterator.next();

                int distance = (int) Point.distance(last.getX(), last.getY(), current.getX(), current.getY());
                if (distance > textCoordLength) {
                    angles[count] = getAngle(last.getX() - current.getX(), last.getY() - current.getY());
                    points[count << 1] = (last.getX() + current.getX()) / 2;
                    points[(count << 1) + 1] = (last.getY() + current.getY()) / 2;
                    ++count;
                }

                last = current;
            }

            return new LabelCandidate(way, points, angles);
        }

        return null;

    }

    private static float getAngle(final double x, final double y) {
        return (float) Math.atan(y / x);
    }
}
