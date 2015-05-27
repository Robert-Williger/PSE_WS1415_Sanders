package model.elements;

import java.awt.Point;

public class Node {

    private int x;
    private int y;

    public Node() {
        this(0, 0);
    }

    public Node(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Point getLocation() {
        return new Point(x, y);
    }

    public void setLocation(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public void setLocation(final Point location) {
        x = location.x;
        y = location.y;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Node other = (Node) obj;
        if (x != other.x && y != other.y) {
            return false;
        }
        return true;
    }

}