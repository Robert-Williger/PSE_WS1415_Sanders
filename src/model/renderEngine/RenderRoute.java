package model.renderEngine;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import util.FloatInterval;

public class RenderRoute implements IRenderRoute {
    private final int length;
    private final Map<Integer, StreetPart> map;
    private final Map<Integer, Collection<FloatInterval>> multiPartMap;
    private final Rectangle bounds;

    public RenderRoute(final int length, final Rectangle bounds) {
        this.length = length;
        this.bounds = bounds;

        map = new HashMap<>(64);
        multiPartMap = new HashMap<>();
    }

    public void addStreet(final int edge) {
        map.put(getEdgeID(edge), new StreetPart(StreetUse.full, null));
    }

    public void addStreetPart(final int edge, final float startIN, final float endIN) {
        FloatInterval intervall;

        if (startIN > endIN) {
            intervall = new FloatInterval(endIN, startIN);
        } else {
            intervall = new FloatInterval(startIN, endIN);
        }

        final int edgeID = getEdgeID(edge);
        final StreetPart mapEntry = map.get(edgeID);

        if (mapEntry == null || mapEntry.useage == StreetUse.none) {
            // not contained yet
            map.put(edgeID, new StreetPart(StreetUse.part, intervall));
            return;
        }

        if (mapEntry.useage == StreetUse.full) {
            // already contained
            return;
        }

        if (mapEntry.useage == StreetUse.part) {
            // one other part already contained

            if (mapEntry.intervall.equals(intervall)) {
                // already contained
                return;
            }

            // // check if merging is possible
            // if (mapEntry.intervall.getEnd() == intervall.getStart()) {
            // // merge
            // intervall = new Intervall(mapEntry.intervall.getStart(),
            // intervall.getEnd());
            // if (intervall.getStart() == 0f && intervall.getEnd() == 1f) {
            // map.put(id, new StreetPart(StreetUse.full, null));
            // } else {
            // map.put(id, new StreetPart(StreetUse.part, intervall));
            // }
            // return;
            // }
            //
            // if (intervall.getEnd() == mapEntry.intervall.getStart()) {
            // // merge
            // intervall = new Intervall(intervall.getStart(),
            // mapEntry.intervall.getEnd());
            // if (intervall.getStart() == 0f && intervall.getEnd() == 1f) {
            // map.put(id, new StreetPart(StreetUse.full, null));
            // } else {
            // map.put(id, new StreetPart(StreetUse.part, intervall));
            // }
            // return;
            // }

            // create multiPart
            final Collection<FloatInterval> multiMapEntry = new LinkedList<>();
            multiMapEntry.add(mapEntry.intervall);
            multiMapEntry.add(intervall);
            multiPartMap.put(edgeID, multiMapEntry);
            map.put(edgeID, new StreetPart(StreetUse.multiPart, null));
        } else if (!multiPartMap.get(edgeID).contains(intervall)) {
            // more than one other part already contained
            multiPartMap.get(edgeID).add(intervall);
        }
    }

    @Override
    public StreetUse getStreetUse(final int id) {
        int edgeID = getEdgeID(id);
        if (map.containsKey(edgeID)) {
            return map.get(edgeID).useage;
        }

        return StreetUse.none;
    }

    @Override
    public FloatInterval getStreetPart(final int id) {
        int edgeID = getEdgeID(id);
        if (map.containsKey(edgeID)) {
            return map.get(edgeID).intervall;
        }

        return null;
    }

    @Override
    public Collection<FloatInterval> getStreetMultiPart(final int id) {
        return multiPartMap.get(getEdgeID(id));
    }

    // TODO own instance for conversation?
    private int getEdgeID(final int id) {
        return id & 0x7FFFFFFF;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    private static class StreetPart {
        StreetUse useage;
        FloatInterval intervall;

        public StreetPart(final StreetUse useage, final FloatInterval intervall) {
            this.useage = useage;
            this.intervall = intervall;
        }
    }

}