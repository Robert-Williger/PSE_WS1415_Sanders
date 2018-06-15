package adminTool.elements;

public class Boundary implements Typeable {

    private static int count;
    private final int id;
    private final String name;
    private final MultiElement[] outlines;
    private final MultiElement[] holes;

    public Boundary(final MultiElement[] outlines, final MultiElement[] holes, final String name) {
        this.outlines = outlines;
        this.name = name;
        this.holes = holes;
        id = count++;
    }

    public MultiElement getOutline(final int index) {
        return outlines[index];
    }

    public int getOutlines() {
        return outlines.length;
    }

    public MultiElement getHole(final int index) {
        return holes[index];
    }

    public int getHoles() {
        return holes.length;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }

    @Override
    public int getType() {
        return outlines[0].getType();
    }
}
