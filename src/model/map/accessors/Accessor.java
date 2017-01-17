package model.map.accessors;

public class Accessor implements IAccessor {

    private long id;

    public Accessor() {
    }

    public long getID() {
        return id;
    }

    @Override
    public void setID(long id) {
        this.id = id;
    }
}
