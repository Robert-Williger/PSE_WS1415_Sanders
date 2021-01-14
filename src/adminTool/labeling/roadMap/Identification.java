package adminTool.labeling.roadMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adminTool.elements.Way;
import adminTool.labeling.INameInfo;

public class Identification {
    private List<LabelSection> roads;
    private String[] names;
    private int roadIds;

    public void identify(final Collection<Way> ways) {
        HashMap<String, Integer> idMap = new HashMap<>();

        roads = new ArrayList<>(ways.size());

        int roadId = 0;
        for (final Way way : ways) {
            String name = getName(way);
            Integer id = idMap.get(name);
            if (id == null) {
                idMap.put(name, roadId);
                id = roadId;
                ++roadId;
            }

            roads.add(new LabelSection(way, way.getType(), id));
        }

        roadIds = roadId;
        names = new String[roadIds];
        for (final Map.Entry<String, Integer> entry : idMap.entrySet()) {
            names[entry.getValue()] = entry.getKey();
        }
    }

    public List<LabelSection> getRoads() {
        return roads;
    }

    public INameInfo getNameInfo() {
        return (id) -> names[id];
    }

    public int getRoadIds() {
        return roadIds;
    }

    private String getName(final Way way) {
        return way.getName() != null ? way.getName() : "";
    }
}
