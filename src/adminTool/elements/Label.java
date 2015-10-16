package adminTool.elements;

public abstract class Label extends Node implements Typeable {

    private String name;
    private int type;

    private Label(final String name, final int type, final int x, final int y) {
        super(x, y);

        this.name = name;
        this.type = type;
    }

    @Override
    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public abstract int getRotation();

    public static Label create(final String name, final int type, final int x, final int y) {
        return new DefaultLabel(name, type, x, y);
    }

    public static Label create(final String name, final int type, final int x, final int y, final int rotation) {
        return new RotatedLabel(name, type, x, y, rotation);
    }

    private static class DefaultLabel extends Label {
        public DefaultLabel(final String name, final int type, final int x, final int y) {
            super(name, type, x, y);
        }

        @Override
        public int getRotation() {
            return 0;
        }
    }

    private static class RotatedLabel extends Label {
        private final int rotation;

        public RotatedLabel(final String name, final int type, final int x, final int y, final int rotation) {
            super(name, type, x, y);
            this.rotation = rotation;
        }

        @Override
        public int getRotation() {
            return rotation;
        }
    }
}
