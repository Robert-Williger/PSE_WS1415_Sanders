package adminTool.labeling.roadGraph;

import java.awt.Dimension;
import java.util.Collection;
import java.util.List;

import adminTool.UnboundedPointAccess;
import adminTool.elements.MultiElement;
import adminTool.elements.Way;
import adminTool.labeling.roadGraph.simplification.Simplification;

public class RoadGraphCreator {

    private final int maxWayCoordWidth;
    private final int simplificationThreshold;
    private final int tThreshold;
    private final int stubThreshold;
    private final int junctionThreshold;

    private List<MultiElement> paths;

    public RoadGraphCreator(final int maxWayCoordWidth, final int simplificationThreshold, final int stubThreshold,
            final int tThreshold, final int junctionThreshold) {
        this.maxWayCoordWidth = maxWayCoordWidth;
        this.simplificationThreshold = simplificationThreshold;
        this.stubThreshold = stubThreshold;
        this.tThreshold = tThreshold;
        this.junctionThreshold = junctionThreshold;
    }

    public List<MultiElement> getPaths() {
        return paths;
    }

    public void createRoadGraph(final Collection<Way> ways, final UnboundedPointAccess points,
            final Dimension mapSize) {
        System.out.println("identifiy");
        final Identification identification = new Identification();
        identification.identify(ways);

        // 161

        System.out.println("simplify");
        final Simplification simplification = new Simplification(maxWayCoordWidth, simplificationThreshold);
        simplification.simplify(identification.getEqualWays(), points);

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

        List<MultiElement> simplifiedPaths = simplification.getPaths();

        // try {
        // DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream("simplification")));
        // UnboundedPointAccess pathPoints = PersistenceUtil.readPoints(input);
        // for (int i = points.getPoints(); i < pathPoints.getPoints(); ++i) {
        // points.addPoint(pathPoints.getX(i), pathPoints.getY(i));
        // }
        // simplifiedPaths = PersistenceUtil.readElements(input);
        // input.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        System.out.println("planarize");
        final Planarization planarization = new Planarization(stubThreshold, tThreshold);
        planarization.planarize(simplifiedPaths, points, mapSize);

        // int count = 0;
        // for (final MultiElement element : planarization.getProcessedPaths()) {
        // if (element.getType() == 151) {
        // System.out.println(element.getNode(0) + " - " + element.getNode(element.size() - 1));
        // ++count;
        // }
        // }
        // System.out.println(count);

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
        final Transformation transformation = new Transformation(maxWayCoordWidth, junctionThreshold);
        transformation.transform(planarization.getProcessedPaths(), points);

        System.out.println("road graph creation done");
        paths = transformation.getProcessedPaths();
    }

}