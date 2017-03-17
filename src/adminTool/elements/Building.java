package adminTool.elements;

public abstract class Building extends Area {

    public static Building create(final Node[] nodes) {
        return new EmptyBuilding(nodes);
    }

    public static Building create(final Node[] nodes, final String street, final String number) {
        return new NamedBuilding(nodes, street, number);
    }

    public static Building create(final Node[] nodes, final StreetNode node, final String houseNumber) {
        return new StreetNodeBuilding(nodes, node, houseNumber);
    }

    protected Building(final Node[] nodes) {
        super(nodes, 0);
    }

    public abstract String getAddress();

    public abstract String getStreet();

    public abstract String getHouseNumber();

    public abstract StreetNode getStreetNode();

    private static class EmptyBuilding extends Building {

        public EmptyBuilding(final Node[] nodes) {
            super(nodes);
        }

        @Override
        public String getStreet() {
            return "";
        }

        @Override
        public String getHouseNumber() {
            return "";
        }

        @Override
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

        public NamedBuilding(final Node[] nodes, final String street, final String number) {
            super(nodes);
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

        public StreetNodeBuilding(final Node[] nodes, final StreetNode node, final String houseNumber) {
            super(nodes);
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