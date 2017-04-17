package adminTool.elements;

public abstract class Building extends Area {

    public static Building create(final Node[] nodes) {
        return new EmptyBuilding(nodes);
    }

    public static Building create(final Node[] nodes, final String street) {
        return new HalfAddressedBuilding(nodes, street);
    }

    public static Building create(final Node[] nodes, final String street, final String number, final String name) {
        if (name != null) {
            return new NamedBuilding(nodes, street, number, name);
        }

        if (street != null) {
            if (number != null) {
                return new AddressedBuilding(nodes, street, number);
            }
            return new HalfAddressedBuilding(nodes, street);
        }

        return new EmptyBuilding(nodes);
    }

    protected Building(final Node[] nodes) {
        super(nodes, 0);
    }

    public abstract String getName();

    public abstract String getStreet();

    public abstract String getHouseNumber();

    private static class EmptyBuilding extends Building {

        public EmptyBuilding(final Node[] nodes) {
            super(nodes);
        }

        @Override
        public String getStreet() {
            return null;
        }

        @Override
        public String getHouseNumber() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }
    }

    private static class NamedBuilding extends AddressedBuilding {
        private final String name;

        public NamedBuilding(final Node[] nodes, final String street, final String number, final String name) {
            super(nodes, street, number);
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static class AddressedBuilding extends HalfAddressedBuilding {
        private final String number;

        public AddressedBuilding(final Node[] nodes, final String street, final String number) {
            super(nodes, street);
            this.number = number;
        }

        @Override
        public String getHouseNumber() {
            return number;
        }
    }

    private static class HalfAddressedBuilding extends EmptyBuilding {
        private final String street;

        public HalfAddressedBuilding(final Node[] nodes, final String street) {
            super(nodes);
            this.street = street;
        }

        @Override
        public String getStreet() {
            return street;
        }
    }
}