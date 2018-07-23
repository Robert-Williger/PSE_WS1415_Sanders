package adminTool.elements;

public interface IPointAccess {

    int size();

    double getX(int index);

    double getY(int index);

    void set(int index, final double x, final double y);
}
