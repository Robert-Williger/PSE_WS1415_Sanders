package model.elements;

public class Area extends MultiElement implements IArea {

    private final int type;

    public Area(final int[] points, final int type) {
        super(points);

        this.type = type;
    }

    @Override
    public int getType() {
        return type;
    }

    public IArea getSubElement(final int[] subarray) {
        return new SubArea(subarray);
    }

    // taken from java.awt.Polygon
    @Override
    public boolean contains(final int x, final int y) {
        if (size() <= 2) {
            return false;
        }
        int hits = 0;

        final int size = size();
        int lastx = getX(size - 1);// [npoints - 1];
        int lasty = getY(size - 1);
        int curx, cury;

        // Walk the edges of the polygon
        for (int i = 0; i < size; lastx = curx, lasty = cury, i++) {
            curx = getX(i);
            cury = getY(i);

            if (cury == lasty) {
                continue;
            }

            int leftx;
            if (curx < lastx) {
                if (x >= lastx) {
                    continue;
                }
                leftx = curx;
            } else {
                if (x >= curx) {
                    continue;
                }
                leftx = lastx;
            }

            double test1, test2;
            if (cury < lasty) {
                if (y < cury || y >= lasty) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - curx;
                test2 = y - cury;
            } else {
                if (y < lasty || y >= cury) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - lastx;
                test2 = y - lasty;
            }

            if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
                hits++;
            }
        }

        return ((hits & 1) != 0);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Area)) {
            return false;
        }
        Area other = (Area) obj;
        if (type != other.type) {
            return false;
        }
        return true;
    }

    private class SubArea implements IArea {
        private final int[] subarray;

        public SubArea(final int[] subarray) {
            this.subarray = subarray;
        }

        @Override
        public int getType() {
            return type;
        }

        @Override
        public boolean contains(final int x, final int y) {
            return Area.this.contains(x, y);
        }

        @Override
        public int size() {
            return subarray.length;
        }

        @Override
        public int getX(final int i) {
            return Area.this.getX(subarray[i]);
        }

        @Override
        public int getY(final int i) {
            return Area.this.getY(subarray[i]);
        }

    }
}