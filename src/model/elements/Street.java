package model.elements;

import java.awt.Point;
import java.util.Iterator;

public class Street extends Way {

    private final long id;
    private int length;

    public Street(final Node[] nodes, final int type, final String name, final long id) {
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

    public long getID() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (id ^ (id >>> 32));
        // no need to add length to hashCode because it is fully depending on
        // the already hashed nodes/street
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Street other = (Street) obj;
        if (id != other.id) {
            return false;
        }
        // no need to compare length because it is fully depending on the
        // already compared nodes/street

        return true;
    }

}