package model.map.accessors;

public class StringAccessor implements IStringAccessor {

    private final String[] strings;
    private int id;

    public StringAccessor(final String[] strings) {
        this.strings = strings;
    }

    @Override
    public void setID(long id) {
        this.id = (int) id;
    }

    @Override
    public String getString() {
        return strings[id];
    }

}
