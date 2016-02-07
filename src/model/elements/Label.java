package model.elements;

public abstract class Label extends Node {

    private String name;
    private int type;

    private Label(final String name, final int type, final int x, final int y) {
        super(x, y);

        this.name = name;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public abstract float getRotation();

    public static Label create(final String name, final int type, final int x, final int y) {
        return new DefaultLabel(name, type, x, y);
    }

    public static Label create(final String name, final int type, final int x, final int y, final float rotation) {
        return new RotatedLabel(name, type, x, y, rotation);
    }

    private static class DefaultLabel extends Label {
        public DefaultLabel(final String name, final int type, final int x, final int y) {
            super(name, type, x, y);
        }

        @Override
        public float getRotation() {
            return 0;
        }
    }

    private static class RotatedLabel extends Label {
        private final float rotation;

        public RotatedLabel(final String name, final int type, final int x, final int y, final float rotation) {
            super(name, type, x, y);
            this.rotation = rotation;
        }

        @Override
        public float getRotation() {
            return rotation;
        }
    }
}
