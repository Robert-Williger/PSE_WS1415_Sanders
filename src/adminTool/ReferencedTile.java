package adminTool;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

public class ReferencedTile {

    public static final ReferencedTile EMPTY_TILE = new ReferencedTile();

    private final TreeSet<Integer> streets;
    private final TreeSet<Integer> ways;
    private final Set<Integer> buildings;
    private final TreeSet<Integer> terrain;
    private final Collection<ReferencedPOI> pois;

    public ReferencedTile() {
        streets = new TreeSet<Integer>();
        ways = new TreeSet<Integer>();
        buildings = new HashSet<Integer>();
        terrain = new TreeSet<Integer>();
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

    public byte getFlags() {
        return (byte) (streetFlag() | wayFlag() | buildingFlag() | poiFlag() | terrainFlag());
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
}