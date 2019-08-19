package adminTool.labeling.algorithm.postprocessing;

import adminTool.labeling.LabelPath;

public class ShiftableLabel extends LabelPath {
    private double headMin;
    private double headMax;

    public ShiftableLabel(LabelPath label, double headMin, double headMax) {
        super(label);
        this.headMin = headMin;
        this.headMax = headMax;
    }

    public void shift(final double distance) {
        setHeadPosition(getHeadPosition() + distance);
        setTailPosition(getTailPosition() + distance);
    }

    public double maxRightShiftableDistance() {
        return headMax - getHeadPosition();
    }

    public double getHeadMin() {
        return headMin;
    }

    public double getHeadMax() {
        return headMax;
    }

    public void setHeadMin(double headMin) {
        this.headMin = headMin;
    }

    public void setHeadMax(double headMax) {
        this.headMax = headMax;
    }

    // @Override
    // public String toString() {
    // return "ShiftableLabel [headMin=" + headMin + ", headMax=" + headMax + ", edgePath=" + getEdgePath()
    // + ", headPosition=" + getHeadPosition() + ", tailPosition=" + getTailPosition() + "]";
    // }

}