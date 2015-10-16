package adminTool.elements;

import java.util.Iterator;

import util.Arrays;

public class MultiElement implements Iterable<Node> {

    protected final Node[] nodes;

    public MultiElement(final Node[] nodes) {
        this.nodes = nodes;
    }

    @Override
    public Iterator<Node> iterator() {
        return Arrays.iterator(nodes);
    }

    public int size() {
        return nodes.length;
    }

    // TODO remove this ?
    public Node[] getNodes() {
        return nodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodes == null) ? 0 : java.util.Arrays.hashCode(nodes));
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
        final MultiElement other = (MultiElement) obj;
        if (nodes == null) {
            if (other.nodes != null) {
                return false;
            }
        } else if (!java.util.Arrays.equals(nodes, other.nodes)) {
            return false;
        }
        return true;
    }

}