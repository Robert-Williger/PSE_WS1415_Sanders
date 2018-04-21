package adminTool.labeling.roadGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import adminTool.elements.Way;

public class Identification {
    private Collection<List<Way>> equalWays;

    public void identify(final Collection<Way> ways) {
        HashMap<String, List<Way>> wayMap = new HashMap<>();
        for (final Way way : ways) {
            if (way.getName() == null || way.getName().isEmpty())
                continue;
            List<Way> equalWays = wayMap.get(way.getType() + way.getName());
            if (equalWays == null) {
                equalWays = new ArrayList<Way>();
                wayMap.put(way.getType() + way.getName(), equalWays);
            }
            equalWays.add(way);
        }

        equalWays = wayMap.values();
    }

    public Collection<List<Way>> getEqualWays() {
        return equalWays;
    }
}
