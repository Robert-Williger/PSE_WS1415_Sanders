package adminTool.elements;

import java.awt.Point;
import java.util.Iterator;

public class Street extends Way {

    private final int id;
    private int length;

    public Street(final Node[] nodes, final int type, final String name, final int id) {
        super(nodes, type, name);
        this.id = id;
    }

    public int getLength() {
        if (length == 0) {
            calculateLength();
        }

        return length;
    }

    private void calculateLength() {
        float totalLength = 0f;
        final Iterator<Node> iterator = iterator();
        Node lastNode = iterator.next();

        while (iterator.hasNext()) {
            final Node currentNode = iterator.next();
            totalLength += Point.distance(currentNode.getX(), currentNode.getY(), lastNode.getX(), lastNode.getY());
            lastNode = currentNode;
        }

        length = (int) totalLength;
    }

    public int getID() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Street)) {
            return false;
        }
        Street other = (Street) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }


}