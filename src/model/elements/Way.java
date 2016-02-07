package model.elements;

public class Way extends MultiElement implements IWay {

    private final int type;
    private final String name;

    @Deprecated
    public Way(final int[] x, final int[] y, final int type, final String name) {
        super(x, y);

        this.type = type;
        this.name = name;
    }

    public Way(final int[] points, final int type, final String name) {
        super(points);

        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public IWay getSubElement(final int[] subarray) {
        return new SubWay(subarray);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + type;
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
        final Way other = (Way) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    private class SubWay implements IWay {

        private final int[] subarray;

        public SubWay(final int[] subarray) {
            this.subarray = subarray;
        }

        @Override
        public int size() {
            return subarray.length;
        }

        @Override
        public int getX(int i) {
            return Way.this.getX(subarray[i]);
        }

        @Override
        public int getY(int i) {
            return Way.this.getY(subarray[i]);
        }

        @Override
        public int getType() {
            return Way.this.getType();
        }

        @Override
        public String getName() {
            return Way.this.getName();
        }

    }
}