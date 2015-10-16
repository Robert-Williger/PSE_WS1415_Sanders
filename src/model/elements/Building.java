package model.elements;

public abstract class Building extends Area {

    public static Building create(final int[] xPoints, final int[] yPoints) {
        return new EmptyBuilding(xPoints, yPoints);
    }

    public static Building create(final int[] xPoints, final int[] yPoints, final String street, final String number) {
        return new NamedBuilding(xPoints, yPoints, street, number);
    }

    public static Building create(final int[] xPoints, final int[] yPoints, final StreetNode node,
            final String houseNumber) {
        return new StreetNodeBuilding(xPoints, yPoints, node, houseNumber);
    }

    protected Building(final int[] xPoints, final int[] yPoints) {
        super(xPoints, yPoints, 0);
    }

    public abstract String getAddress();

    public abstract String getStreet();

    public abstract String getHouseNumber();

    public abstract StreetNode getStreetNode();

    private static class EmptyBuilding extends Building {

        public EmptyBuilding(final int[] xPoints, final int[] yPoints) {
            super(xPoints, yPoints);
        }

        public String getStreet() {
            return "";
        }

        public String getHouseNumber() {
            return "";
        }

        public StreetNode getStreetNode() {
            return null;
        }

        @Override
        public String getAddress() {
            return "";
        }
    }

    private static class NamedBuilding extends Building {
        private final String street;
        private final String number;

        public NamedBuilding(final int[] xPoints, final int[] yPoints, final String street, final String number) {
            super(xPoints, yPoints);
            this.street = street;
            this.number = number;
        }

        @Override
        public String getAddress() {
            return getStreet() + " " + getHouseNumber();
        }

        @Override
        public String getStreet() {
            return street;
        }

        @Override
        public String getHouseNumber() {
            return number;
        }

        @Override
        public StreetNode getStreetNode() {
            return null;
        }
    }

    private static class StreetNodeBuilding extends Building {

        private final StreetNode node;
        private final String houseNumber;

        public StreetNodeBuilding(final int[] xPoints, final int[] yPoints, final StreetNode node,
                final String houseNumber) {
            super(xPoints, yPoints);
            this.node = node;
            this.houseNumber = houseNumber;
        }

        @Override
        public String getAddress() {
            return getStreet() + " " + getHouseNumber();
        }

        @Override
        public String getStreet() {
            return node.getStreet().getName();
        }

        @Override
        public String getHouseNumber() {
            return houseNumber;
        }

        @Override
        public StreetNode getStreetNode() {
            return node;
        }
    }
}