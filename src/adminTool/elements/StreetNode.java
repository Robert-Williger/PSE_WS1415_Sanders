package adminTool.elements;

public class StreetNode {

    private final float offset;
    private Street street;

    public StreetNode(final float offset, final Street street) {
        this.offset = Math.max(0, Math.min(1, offset));
        setStreet(street);
    }

    public float getOffset() {
        return offset;
    }

    public Street getStreet() {
        return street;
    }

    public void setStreet(final Street street) {
        this.street = street;
    }
}