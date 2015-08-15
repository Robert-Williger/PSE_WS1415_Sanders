package adminTool.configurations;

public class PerformantConfiguration implements IElementOrder {

    private final int[] terrain;
    private final int[] street;
    private final int[] way;
    private final int[] poi;

    public PerformantConfiguration() {
        terrain = new int[]{7, 1, 22, 3, 4, 2, 9, 12, 16, 10, 6, 8, 13, 19, 21, 17, 15, 0, 23, 18, 20, 11, 5, 14};
        street = new int[]{0, 1, 3, 2, 4, 5, 6, 7, 8, 9};
        way = new int[]{10, 11, 19, 17, 18, 12, 22, 23, 16, 14, 13, 15, 20, 21};
        poi = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
    }

    @Override
    public int[] getTerrainOrder() {
        return terrain;
    }

    @Override
    public int[] getStreetOrder() {
        return street;
    }

    @Override
    public int[] getWayOrder() {
        return way;
    }

    @Override
    public int[] getPOIOrder() {
        return poi;
    }

}
