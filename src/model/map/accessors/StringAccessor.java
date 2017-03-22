package model.map.accessors;

public class StringAccessor implements IStringAccessor {

    private final String[] strings;

    public StringAccessor(final String[] strings) {
        this.strings = strings;
    }

    @Override
    public String getString(final long id) {
        return strings[(int) id];
    }

}
