package adminTool.labeling.algorithm;

import java.util.Collection;

import adminTool.labeling.LabelPath;
import adminTool.labeling.roadMap.RoadMap;

public interface IRoadMapLabelAlgorithm {

    void calculateLabeling(final RoadMap roadMap);

    Collection<LabelPath> getLabeling();
}
