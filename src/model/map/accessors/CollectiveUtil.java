package model.map.accessors;

import java.awt.Point;

public class CollectiveUtil {

    // TODO
    public static int getLength(final ICollectiveAccessor accessor) {
        float totalLength = 0f;

        int lastX = accessor.getX(0);
        int lastY = accessor.getY(0);
        int size = accessor.size();
        for (int i = 1; i < size; i++) {
            int currentX = accessor.getX(i);
            int currentY = accessor.getY(i);
            totalLength += Point.distance(currentX, currentY, lastX, lastY);
            lastX = currentX;
            lastY = currentY;
        }

        return (int) totalLength;
    }

    public static boolean contains(final ICollectiveAccessor accessor, final int x, final int y) {
        if (!containsBB(accessor, x, y)) {
            return false;
        }

        final int size = accessor.size();
        boolean c = false;
        for (int i = 0, j = size - 1; i < size; j = i++) {
            if (((accessor.getY(i) > y) != (accessor.getY(j) > y)) && (x < (accessor.getX(j) - accessor.getX(i))
                    * (y - accessor.getY(i)) / (accessor.getY(j) - accessor.getY(i)) + accessor.getX(i)))
                c = !c;
        }
        return c;
    }

    public static boolean containsBB(final ICollectiveAccessor accessor, final int x, final int y) {
        final int size = accessor.size();

        int lowX = accessor.getX(0);
        int highX = lowX;
        int lowY = accessor.getY(0);
        int highY = lowY;
        for (int i = 1; i < size; i++) {
            lowX = Math.min(lowX, accessor.getX(i));
            highX = Math.max(highX, accessor.getX(i));
            lowY = Math.min(lowY, accessor.getY(i));
            highY = Math.max(highY, accessor.getY(i));
        }

        return x >= lowX && x <= highX && y >= lowY && y <= highY;
    }

    public static Point getLocation(final ICollectiveAccessor accessor, final float offset) {
        final int size = accessor.size();

        final float totalLength = getLength(accessor);
        final float maxLength = totalLength * offset;

        int lastX = accessor.getX(0);
        int lastY = accessor.getY(0);
        float currentOffsetLength = 0f;

        for (int i = 1; i < size; i++) {
            final int currentX = accessor.getX(i);
            final int currentY = accessor.getY(i);
            final double distance = Point.distance(lastX, lastY, currentX, currentY);

            if (currentOffsetLength + distance > maxLength || i == size - 1) {
                final int xDistance = currentX - lastX;
                final int yDistance = currentY - lastY;

                final float partOffsetLength = maxLength - currentOffsetLength;
                final float partOffset = (float) (partOffsetLength / distance);

                return new Point((int) (lastX + xDistance * partOffset + 0.49f),
                        (int) (lastY + yDistance * partOffset + 0.49f));
            }

            currentOffsetLength += distance;
            lastX = currentX;
            lastY = currentY;
        }

        return new Point();
    }

}
