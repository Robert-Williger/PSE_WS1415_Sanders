package adminTool;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

public class ReferencedTile {

    public static final ReferencedTile EMPTY_TILE = new ReferencedTile();

    private final Set<Integer> streets;
    private final Set<Integer> ways;
    private final Set<Integer> buildings;
    private final Set<Integer> terrain;

    private final Collection<ReferencedPoint> pois;
    private final Collection<ReferencedRectangle> labels;

    public ReferencedTile() {
        streets = new TreeSet<Integer>();
        ways = new TreeSet<Integer>();
        buildings = new HashSet<Integer>();
        terrain = new TreeSet<Integer>();
        pois = new LinkedList<ReferencedPoint>();
        labels = new LinkedList<ReferencedRectangle>();
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

    public Collection<ReferencedPoint> getPOIs() {
        return pois;
    }

    public Collection<ReferencedRectangle> getLabels() {
        return labels;
    }

    public byte getFlags() {
        return (byte) (streetFlag() | wayFlag() | buildingFlag() | poiFlag() | terrainFlag() | labelFlag());
    }

    private int poiFlag() {
        return pois.isEmpty() ? 0b00000000 : 0b00000001;
    }

    private int streetFlag() {
        return streets.isEmpty() ? 0b00000000 : 0b00000010;
    }

    private int wayFlag() {
        return ways.isEmpty() ? 0b00000000 : 0b00000100;
    }

    private int buildingFlag() {
        return buildings.isEmpty() ? 0b00000000 : 0b00001000;
    }

    private int terrainFlag() {
        return terrain.isEmpty() ? 0b00000000 : 0b00010000;
    }

    private int labelFlag() {
        return labels.isEmpty() ? 0b00000000 : 0b00100000;
    }
}