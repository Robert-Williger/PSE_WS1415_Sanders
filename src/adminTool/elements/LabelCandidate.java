package adminTool.elements;

public class LabelCandidate {

    public static final int HEIGHT = 20;
    private final IWay way;
    private final int[] points;
    private final float[] angles;

    public LabelCandidate(final IWay way, final int[] points, final float[] angles) {
        this.way = way;
        this.points = points;
        this.angles = angles;
    }

    public IWay getWay() {
        return way;
    }

    public int getX(final int index) {
        return points[index >> 1];
    }

    public int getY(final int index) {
        return points[(index >> 1) + 1];
    }

    public float getAngle(final int index) {
        return angles[index];
    }

    public int size() {
        return angles.length;
    }

}
