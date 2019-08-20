package adminTool.labeling.algorithm;

import java.util.ArrayList;
import java.util.Collection;

import adminTool.elements.LineLabel;
import adminTool.elements.PointAccess;
import adminTool.labeling.LabelPath;
import adminTool.labeling.roadMap.RoadMap;

public class Postprocessor {

    private final PointAccess points;
    private Collection<LineLabel> labeling;

    public Postprocessor(PointAccess points) {
        super();
        this.points = points;
    }

    public void postprocess(final RoadMap roadMap, final Collection<LabelPath> labeling, final int zoom) {
        this.labeling = new ArrayList<LineLabel>(labeling.size());
        LabelCreator labelCreator = new LabelCreator(roadMap, points);
        for (final LabelPath path : labeling) {
            this.labeling.add(labelCreator.createLabel(path, zoom));
        }
    }

    public Collection<LineLabel> getLabeling() {
        return labeling;
    }
}
