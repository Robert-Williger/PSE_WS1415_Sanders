package model.elements;

public abstract class Building extends Area implements IBuilding {

    public static Building create(final int[] points) {
        return new EmptyBuilding(points);
    }

    public static Building create(final int[] points, final String street, final String number) {
        return new NamedBuilding(points, street, number);
    }

    public static Building create(final int[] points, final StreetNode node, final String houseNumber) {
        return new StreetNodeBuilding(points, node, houseNumber);
    }

    protected Building(final int[] points) {
        // TODO get other types
        super(points, 0);
    }

    private static class EmptyBuilding extends Building {

        public EmptyBuilding(final int[] points) {
            super(points);
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

        public NamedBuilding(final int[] points, final String street, final String number) {
            super(points);
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

        public StreetNodeBuilding(final int[] points, final StreetNode node, final String houseNumber) {
            super(points);
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