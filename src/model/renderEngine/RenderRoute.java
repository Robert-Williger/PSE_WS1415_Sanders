package model.renderEngine;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import model.targets.IPointList;

public class RenderRoute implements IRenderRoute {
    private final int length;
    private final Map<Long, StreetPart> map;
    private final Map<Long, Collection<Intervall>> multiPartMap;
    private final Rectangle bounds;
    private final IPointList pointList;

    private class StreetPart {
        StreetUse useage;
        Intervall intervall;

        public StreetPart(final StreetUse useage, final Intervall intervall) {
            this.useage = useage;
            this.intervall = intervall;
        }
    }

    public RenderRoute(final int length, final Rectangle bounds, final IPointList pointList) {
        this.length = length;
        this.bounds = bounds;
        this.pointList = pointList;

        map = new HashMap<Long, StreetPart>(64);
        multiPartMap = new HashMap<Long, Collection<Intervall>>();
    }

    public void addStreet(final long id) {
        map.put(id, new StreetPart(StreetUse.full, null));
    }

    public void addStreetPart(final long id, final float startIN, final float endIN) {
        Intervall intervall;

        if (startIN > endIN) {
            intervall = new Intervall(endIN, startIN);
        } else {
            intervall = new Intervall(startIN, endIN);
        }

        final StreetPart mapEntry = map.get(id);

        if (mapEntry == null || mapEntry.useage == StreetUse.none) {
            // not contained yet
            map.put(id, new StreetPart(StreetUse.part, intervall));
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
            final Collection<Intervall> multiMapEntry = new LinkedList<Intervall>();
            multiMapEntry.add(mapEntry.intervall);
            multiMapEntry.add(intervall);
            multiPartMap.put(id, multiMapEntry);
            map.put(id, new StreetPart(StreetUse.multiPart, null));
        } else if (!multiPartMap.get(id).contains(intervall)) {
            // more than one other part already contained
            multiPartMap.get(id).add(intervall);
        }
    }

    @Override
    public StreetUse getStreetUse(final long id) {
        if (map.containsKey(id)) {
            return map.get(id).useage;
        } else {
            return StreetUse.none;
        }
    }

    @Override
    public Intervall getStreetPart(final long id) {
        if (map.containsKey(id)) {
            return map.get(id).intervall;
        }
        return null;
    }

    @Override
    public Collection<Intervall> getStreetMultiPart(final long id) {
        return multiPartMap.get(id);
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public IPointList getPointList() {
        return pointList;
    }

}