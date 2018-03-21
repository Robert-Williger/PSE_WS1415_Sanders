package adminTool.elements;

public abstract class Label extends POI {

    private String name;

    private Label(final int index, final int type, final String name) {
        super(index, type);

        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract float getRotation();

    public static Label create(final int index, final int type, final String name) {
        return new DefaultLabel(index, type, name);
    }

    public static Label create(final int index, final int type, final String name, final float rotation) {
        return new RotatedLabel(index, type, name, rotation);
    }

    private static class DefaultLabel extends Label {
        public DefaultLabel(final int index, final int type, final String name) {
            super(index, type, name);
        }

        @Override
        public float getRotation() {
            return 0;
        }
    }

    private static class RotatedLabel extends Label {
        private final float rotation;

        public RotatedLabel(final int index, final int type, final String name, final float rotation) {
            super(index, type, name);
            this.rotation = rotation;
        }

        @Override
        public float getRotation() {
            return rotation;
        }
    }
}
