package adminTool.labeling.algorithm.postprocessing;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;

import adminTool.labeling.Embedding;
import adminTool.labeling.LabelPath;
import adminTool.labeling.roadMap.RoadMap;
import adminTool.util.ElementAdapter;
import adminTool.util.Vector2D;

public class LabelSpinner {

    public void postprocess(final RoadMap roadMap, final Collection<LabelPath> labeling) {
        final Embedding embedding = roadMap.getEmbedding();
        final LabelReverser reverser = new LabelReverser(roadMap.getGraph());
        final ElementAdapter adapter = new ElementAdapter(roadMap.getEmbedding().getPoints());
        for (final LabelPath label : labeling) {
            adapter.setMultiElement(label.toElement(embedding));

            final Iterator<Point2D> iterator = adapter.iterator();

            final Point2D lastPoint = iterator.next();
            final Point2D curPoint = new Point2D.Double();

            final Vector2D vertical = new Vector2D(0, 1);
            final Vector2D curVec = new Vector2D();

            double correctLen = 0;
            double totalLen = 0;
            while (iterator.hasNext()) {
                curPoint.setLocation(iterator.next());
                curVec.setVector(curPoint.getX() - lastPoint.getX(), curPoint.getY() - lastPoint.getY());
                final double angle = Vector2D.angle(vertical, curVec);
                final double len = curVec.norm();
                correctLen += angle > 0 ? len : 0;
                totalLen += len;

                lastPoint.setLocation(curPoint);
            }

            if (totalLen - correctLen < correctLen)
                reverser.reverse(label);
        }
    }
}
