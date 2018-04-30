package adminTool.labeling.roadGraph;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import adminTool.UnboundedPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;
import adminTool.labeling.roadGraph.simplification.Simplification;
import adminTool.util.PersistenceUtil;

public class RoadGraphCreator {

    private final int maxWayCoordWidth;
    private final int threshold;
    private final int tBias;
    private final int stubThreshold;

    private List<MultiElement> paths;

    public RoadGraphCreator(final int maxWayCoordWidth, final int simplificationThreshold, final int stubThreshold,
            final int tBias) {
        this.maxWayCoordWidth = maxWayCoordWidth;
        this.threshold = simplificationThreshold;
        this.stubThreshold = stubThreshold;
        this.tBias = tBias;
    }

    public List<MultiElement> getPaths() {
        return paths;
    }

    public void createRoadGraph(final Collection<Way> ways, final UnboundedPointAccess points,
            final Dimension mapSize) {

        System.out.println("identifiy");
        // final Identification identification = new Identification();
        // identification.identify(ways);

        System.out.println("simplify");
        // final Simplification simplification = new Simplification(maxWayCoordWidth, threshold);
        // simplification.simplify(identification.getEqualWays(), points);

        // try {
        // final DataOutputStream output = new DataOutputStream(
        // new BufferedOutputStream(new FileOutputStream("simplification")));
        // PersistenceUtil.write(output, simplification.getPoints());
        // PersistenceUtil.write(output, simplification.getPaths());
        // output.flush();
        // output.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        List<MultiElement> simplifiedPaths = null;
        try {
            DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream("simplification")));
            UnboundedPointAccess pathPoints = PersistenceUtil.readPoints(input);
            for (int i = points.getPoints(); i < pathPoints.getPoints(); ++i) {
                points.addPoint(pathPoints.getX(i), pathPoints.getY(i));
            }
            simplifiedPaths = PersistenceUtil.readElements(input);
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("planarize");
        // long start = System.currentTimeMillis();
        final Planarization planarization = new Planarization(stubThreshold, tBias);
        planarization.planarize(simplifiedPaths, points, mapSize);
        // System.out.println(System.currentTimeMillis() - start);

        // try {
        // final DataOutputStream output = new DataOutputStream(
        // new BufferedOutputStream(new FileOutputStream("planarization")));
        // PersistenceUtil.write(output, roadSimplifier.getPoints());
        // PersistenceUtil.write(output, planarization.getProcessedPaths());
        // output.flush();
        // output.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        // List<MultiElement> planarPaths = null;
        // try {
        // DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream("planarization")));
        // UnboundedPointAccess pathPoints = PersistenceUtil.readPoints(input);
        // for (int i = points.getPoints(); i < pathPoints.getPoints(); ++i) {
        // points.addPoint(pathPoints.getX(i), pathPoints.getY(i));
        // }
        // planarPaths = PersistenceUtil.readElements(input);
        // input.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        System.out.println("transform");
        final Transformation transformation = new Transformation(maxWayCoordWidth, threshold);
        transformation.transform(planarization.getProcessedPaths(), points);

        System.out.println("road graph creation done");
        paths = transformation.getProcessedPaths();
    }

}