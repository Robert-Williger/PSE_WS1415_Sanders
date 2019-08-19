package adminTool.labeling;

import java.util.ArrayList;
import java.util.List;

import adminTool.elements.IPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.PointAccess;
import adminTool.labeling.roadMap.LabelSection;
import adminTool.util.Vector2D;
import util.IntList;
import util.IntegerInterval;

public class QualityMeasure {
    private static final double ALPHA_MAX = 22.5 / 180 * Math.PI; // 22.5°

    private IPointAccess points;
    private final double lMax; // size of window sliding along the road
    private final double alphaMax;

    private double[] angles; // angle between segments [n-1,n] and [n,n+1]; zero for border nodes
    private double[] distance;// accumulated length of segments [0,1] ... [n-1,n]; zero for n = 0

    private int lws;// left window border segment
    private int rws;// right window border segment
    private double wp;// window position - geodesic distance of right border to road head
    private double alphaSum; // current sum of bend angles
    private int segments;

    public QualityMeasure(final IPointAccess points, final double lMax) {
        this(points, lMax, ALPHA_MAX);
    }

    public QualityMeasure(final IPointAccess points, final double lMax, final double alphaMax) {
        this.lMax = lMax;
        this.alphaMax = alphaMax;
        this.angles = new double[0];
        this.distance = new double[0];
        this.points = points;
    }

    public boolean isWellShaped(final MultiElement element) {
        initFields(element);

        do {
            moveWindow();
            if (alphaSum > alphaMax)
                return false;
        } while (rws < segments);

        return true;
    }

    public boolean hasWellShapedPiece(final MultiElement element, final double destLabelLength) {
        initFields(element);

        double labelLength = 0; // length of current well shaped label

        while (true) {
            do {
                labelLength += moveWindow();
                if (labelLength >= destLabelLength)
                    return true;
                if (rws == segments)
                    return false;
            } while (alphaSum <= alphaMax);

            // move until alphaSum <= alphaMax
            do {
                moveWindow();
                if (rws == segments)
                    return false;
            } while (alphaSum > alphaMax);

            labelLength = lMax;
        }
    }

    public List<IntegerInterval> getAllMaximumWellShapedIntervals(final MultiElement element) {
        final List<IntegerInterval> ret = new ArrayList<>();
        initFields(element);

        int lls = 0;// left label border segment [rls = rws]

        while (true) {
            do {
                moveWindow();
                if (rws == segments) {
                    ret.add(new IntegerInterval(lls, rws));
                    return ret;
                }
            } while (alphaSum <= alphaMax);

            ret.add(new IntegerInterval(lls, rws));

            do {
                moveWindow();
                if (rws == segments)
                    return ret;
            } while (alphaSum > alphaMax);

            lls = lws;
        }
    }

    private double moveWindow() {
        double d1 = distance[lws + 1] - wp + lMax; // distance for start segment change
        double d2 = distance[rws + 1] - wp; // distance for end segment change

        if (d2 >= d1) {
            alphaSum -= angles[++lws];
            wp += d1;
            return d1;
        } else {
            alphaSum += angles[++rws];
            wp += d2;
            return d2;
        }
    }

    protected void initFields(final MultiElement road) {
        if (this.angles.length < road.size()) {
            this.angles = new double[road.size()];
            this.distance = new double[road.size()];
        }

        final Vector2D last = new Vector2D(points.getX(road.getPoint(0)) - points.getX(road.getPoint(1)),
                points.getY(road.getPoint(0)) - points.getY(road.getPoint(1)));
        final Vector2D current = new Vector2D();

        for (int i = 1; i < road.size() - 1; ++i) {
            current.setVector(points.getX(road.getPoint(i + 1)) - points.getX(road.getPoint(i)),
                    points.getY(road.getPoint(i + 1)) - points.getY(road.getPoint(i)));

            distance[i] = distance[i - 1] + last.norm();
            angles[i] = Math.PI - Math.abs(Vector2D.angle(last, current));

            last.setVector(-current.x(), -current.y());
        }
        distance[road.size() - 1] = distance[road.size() - 2] + last.norm();
        angles[road.size() - 1] = 0;

        lws = -1;
        rws = 0;
        wp = 0;
        alphaSum = 0;
        segments = road.size() - 1;
    }

    public static void main(String[] args) {
        PointAccess points = new PointAccess();
        points.addPoint(0, 0);
        points.addPoint(1, 0);
        points.addPoint(3, 0);
        points.addPoint(3, 1);
        points.addPoint(8, 1);
        final IntList indices = new IntList();
        for (int i = 0; i < points.size(); ++i) {
            indices.add(i);
        }
        final LabelSection road = new LabelSection(indices, 0, 0);

        final double alphaMax = 22.5 / 180 * Math.PI; // 22.5°
        final int lMax = 2;
        final QualityMeasure test = new QualityMeasure(points, lMax, alphaMax);

        final List<IntegerInterval> roads = test.getAllMaximumWellShapedIntervals(road);
        for (final IntegerInterval interval : roads) {
            System.out.println("[" + interval.getStart() + ", " + interval.getEnd() + "]");
        }
    }
}
