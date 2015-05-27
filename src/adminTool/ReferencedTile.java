package adminTool;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class ReferencedTile {

    private final Set<Integer> streets;
    private final Set<Integer> ways;
    private final Set<Integer> buildings;
    private final Set<Integer> terrain;
    private final Collection<ReferencedPOI> pois;

    public ReferencedTile() {
        streets = new HashSet<Integer>();
        ways = new HashSet<Integer>();
        buildings = new HashSet<Integer>();
        terrain = new HashSet<Integer>();
        pois = new LinkedList<ReferencedPOI>();
    }

    public Set<Integer> getStreets() {
        return streets;
    }

    public Set<Integer> getWays() {
        return ways;
    }

    public Set<Integer> getBuildings() {
        return buildings;
    }

    public Set<Integer> getTerrain() {
        return terrain;
    }

    public Collection<ReferencedPOI> getPOIs() {
        return pois;
    }

}