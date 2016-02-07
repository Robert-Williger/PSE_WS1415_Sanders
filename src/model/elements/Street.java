package model.elements;

import java.awt.Point;

public class Street extends Way implements IStreet {

    private final int id;
    private final int length;

    // TODO avoid storage of empty street name
    public Street(final int[] points, final int type, final String name, final int id, final boolean oneway) {
        super(points, type, name);
        this.length = calculateLength();
        if (!oneway) {
            this.id = id;
        } else {
            this.id = id | 0x80000000;
        }
    }

    public Street(final int[] points, final int type, final String name, final int id) {
        this(points, type, name, id, false);
    }

    @Deprecated
    public Street(final int[] x, final int[] y, final int type, final String name, final int id, final boolean oneway) {
        super(x, y, type, name);
        this.length = calculateLength();
        if (!oneway) {
            this.id = id;
        } else {
            this.id = id | 0x80000000;
        }
    }

    @Deprecated
    public Street(final int[] x, final int[] y, final int type, final String name, final int id) {
        this(x, y, type, name, id, false);
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public IStreet getSubElement(final int[] subarray) {
        return new SubStreet(subarray);
    }

    private int calculateLength() {
        if (size() > 1) {
            float totalLength = 0f;

            int lastX = getX(0);
            int lastY = getY(0);

            for (int i = 1; i < size(); i++) {
                int currentX = getX(i);
                int currentY = getY(i);
                totalLength += Point.distance(currentX, currentY, lastX, lastY);
                lastX = currentX;
                lastY = currentY;
            }

            return (int) totalLength;
        }

        return 0;
    }

    @Override
    public int getID() {
        return id & 0x7FFFFFFF;
    }

    @Override
    public boolean isOneWay() {
        return id >>> 31 == 1;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (id ^ (id >>> 32));
        // no need to add length to hashCode because it is fully depending on
        // the already hashed nodes/street
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Street other = (Street) obj;
        if (id != other.id) {
            return false;
        }

        return true;
    }

    private class SubStreet implements IStreet {
        private final int[] subarray;
        // TODO store me?
        private final int length;

        public SubStreet(final int[] subarray) {
            this.subarray = subarray;
            this.length = calculateLength();
        }

        private int calculateLength() {
            if (size() > 1) {
                float totalLength = 0f;

                int lastX = getX(0);
                int lastY = getY(0);

                for (int i = 1; i < size(); i++) {
                    int currentX = getX(i);
                    int currentY = getY(i);
                    totalLength += Point.distance(currentX, currentY, lastX, lastY);
                    lastX = currentX;
                    lastY = currentY;
                }

                return (int) totalLength;
            }

            return 0;
        }

        @Override
        public int getType() {
            return Street.this.getType();
        }

        @Override
        public String getName() {
            return Street.this.getName();
        }

        @Override
        public int size() {
            return subarray.length;
        }

        @Override
        public int getX(final int i) {
            return Street.this.getX(subarray[i]);
        }

        @Override
        public int getY(final int i) {
            return Street.this.getY(subarray[i]);
        }

        @Override
        public int getLength() {
            // return Street.this.getLength();
            // TODO improve this?
            return length;
        }

        @Override
        public int getID() {
            return Street.this.getID();
        }

        @Override
        public boolean isOneWay() {
            return Street.this.isOneWay();
        }

    }

}