package adminTool.labeling.roadGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adminTool.elements.IPointAccess;
import adminTool.elements.PointAccess;
import adminTool.elements.Way;
import adminTool.labeling.INameInfo;

public class Identification {
    private List<Road> roads;
    private PointAccess points;
    private String[] names;
    private int roadIds;

    public void identify(final Collection<Way> ways, final IPointAccess origPoints) {
        HashMap<String, Integer> idMap = new HashMap<>();

        points = new PointAccess(origPoints.size());
        roads = new ArrayList<>(ways.size());

        for (int i = 0; i < origPoints.size(); ++i) {
            points.addPoint(origPoints.getX(i), origPoints.getY(i));
        }

        int roadId = 0;
        for (final Way way : ways) {
            if (way.getName() == null || way.getName().isEmpty())
                continue;
            Integer id = idMap.get(way.getName());
            if (id == null) {
                idMap.put(way.getName(), roadId);
                id = roadId;
                ++roadId;
            }

            roads.add(new Road(way, way.getType(), id));
        }

        roadIds = roadId;
        names = new String[roadIds];
        for (final Map.Entry<String, Integer> entry : idMap.entrySet()) {
            names[entry.getValue()] = entry.getKey();
        }
    }

    public PointAccess getPoints() {
        return points;
    }

    public List<Road> getRoads() {
        return roads;
    }

    public INameInfo getNameInfo() {
        return (id) -> names[id];
    }

    public int getRoadIds() {
        return roadIds;
    }
}
