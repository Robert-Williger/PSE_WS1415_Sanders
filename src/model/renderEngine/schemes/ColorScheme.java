package model.renderEngine.schemes;

import model.renderEngine.schemes.styles.ShapeStyle;
import model.renderEngine.schemes.styles.WayStyle;

public class ColorScheme {

    private final WayStyle[] wayStyles;
    private final int[][] wayOrder;

    private final ShapeStyle[] areaStyles;
    private final int[] areaOrder;

    private final ShapeStyle[] buildingStyles;

    public ColorScheme(final WayStyle[] wayStyles, final ShapeStyle[] areaStyles, final ShapeStyle[] buildingStyles,
            final int[][] wayOrder, final int[] areaOrder) {
        this.wayStyles = wayStyles;
        this.areaOrder = areaOrder;
        this.wayOrder = wayOrder;
        this.areaStyles = areaStyles;
        this.buildingStyles = buildingStyles;
    }

    public WayStyle[] getWayStyles() {
        return wayStyles;
    }

    public ShapeStyle[] getAreaStyles() {
        return areaStyles;
    }

    public ShapeStyle[] getBuildingStyles() {
        return buildingStyles;
    }

    public int[] getAreaOrder() {
        return areaOrder;
    }

    public int[][] getWayOrder() {
        return wayOrder;
    }

}
