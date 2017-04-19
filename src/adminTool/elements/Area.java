package adminTool.elements;

public class Area extends MultiElement implements Typeable {

    private final int type;

    public Area(final Node[] nodes, final int type) {
        super(nodes);

        this.type = type;
    }

    @Override
    public int getType() {
        return type;
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

}