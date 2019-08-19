package adminTool.labeling.algorithm;

import java.util.ArrayList;
import java.util.Collection;

import adminTool.elements.PointAccess;
import adminTool.labeling.Label;
import adminTool.labeling.LabelPath;
import adminTool.labeling.roadMap.RoadMap;

public class Postprocessor {

    private final PointAccess points;
    private Collection<Label> labeling;

    public Postprocessor(PointAccess points) {
        super();
        this.points = points;
    }

    public void postprocess(final RoadMap roadMap, final Collection<LabelPath> labeling) {
        this.labeling = new ArrayList<Label>(labeling.size());
        LabelCreator labelCreator = new LabelCreator(roadMap, points);
        for (final LabelPath path : labeling) {
            this.labeling.add(labelCreator.createLabel(path));
        }
    }

    public Collection<Label> getLabeling() {
        return labeling;
    }
}
