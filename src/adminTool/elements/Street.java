package adminTool.elements;

public class Street extends Way {

    private int id;

    public Street(final Node[] nodes, final int type, final String name) {
        super(nodes, type, name);
    }

    public void setID(final int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + id;
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
        if (!(obj instanceof Street)) {
            return false;
        }
        Street other = (Street) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

}