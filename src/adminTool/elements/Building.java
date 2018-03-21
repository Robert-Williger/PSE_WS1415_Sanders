package adminTool.elements;

public abstract class Building extends MultiElement {

    public static Building create(final int[] indices) {
        return new EmptyBuilding(indices);
    }

    public static Building create(final int[] indices, final String street) {
        return new HalfAddressedBuilding(indices, street);
    }

    public static Building create(final int[] indices, final String street, final String number, final String name) {
        if (name != null) {
            return new NamedBuilding(indices, street, number, name);
        }

        if (street != null) {
            if (number != null) {
                return new AddressedBuilding(indices, street, number);
            }
            return new HalfAddressedBuilding(indices, street);
        }

        return new EmptyBuilding(indices);
    }

    protected Building(final int[] indices) {
        super(indices, 0);
    }

    public abstract String getName();

    public abstract String getStreet();

    public abstract String getHouseNumber();

    private static class EmptyBuilding extends Building {

        public EmptyBuilding(final int[] indices) {
            super(indices);
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

        public NamedBuilding(final int[] indices, final String street, final String number, final String name) {
            super(indices, street, number);
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static class AddressedBuilding extends HalfAddressedBuilding {
        private final String number;

        public AddressedBuilding(final int[] indices, final String street, final String number) {
            super(indices, street);
            this.number = number;
        }

        @Override
        public String getHouseNumber() {
            return number;
        }
    }

    private static class HalfAddressedBuilding extends EmptyBuilding {
        private final String street;

        public HalfAddressedBuilding(final int[] indices, final String street) {
            super(indices);
            this.street = street;
        }

        @Override
        public String getStreet() {
            return street;
        }
    }
}