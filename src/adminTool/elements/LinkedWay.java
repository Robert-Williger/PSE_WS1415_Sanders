package adminTool.elements;

import java.util.Iterator;
import java.util.LinkedList;

import util.Arrays;

public class LinkedWay implements IWay {

    private LinkedList<IWay> ways;

    public LinkedWay() {
        ways = new LinkedList<IWay>();
    }

    @Override
    public Iterator<Node> iterator() {
        return new NodeIterator();
    }

    @Override
    public Iterator<Node> descendingIterator() {
        return new DescendingNodeIterator();
    }

    @Override
    public int getType() {
        return ways.getFirst().getType();
    }

    @Override
    public String getName() {
        return ways.getFirst().getName();
    }

    public void addFirst(final IWay way, final boolean forward) {
        ways.addFirst(getDirectedWay(way, forward));
    }

    public void addLast(final IWay way, final boolean forward) {
        ways.addLast(getDirectedWay(way, forward));
    }

    private IWay getDirectedWay(final IWay way, final boolean forward) {
        return forward ? way : new BackwardWay(way);
    }

    private class NodeIterator implements Iterator<Node> {

        private Iterator<IWay> wayIterator;
        private Iterator<Node> nodeIterator;

        public NodeIterator() {
            wayIterator = ways.iterator();
            nodeIterator = Arrays.iterator();
        }

        @Override
        public boolean hasNext() {
            return nodeIterator.hasNext() || wayIterator.hasNext();
        }

        @Override
        public Node next() {
            if (!nodeIterator.hasNext()) {
                nodeIterator = wayIterator.next().iterator();
                return next();
            }
            return nodeIterator.next();
        }

    }

    private class DescendingNodeIterator implements Iterator<Node> {

        private Iterator<IWay> wayIterator;
        private Iterator<Node> nodeIterator;

        public DescendingNodeIterator() {
            wayIterator = ways.descendingIterator();
            nodeIterator = Arrays.iterator();
        }

        @Override
        public boolean hasNext() {
            return nodeIterator.hasNext() || wayIterator.hasNext();
        }

        @Override
        public Node next() {
            if (!nodeIterator.hasNext()) {
                nodeIterator = wayIterator.next().descendingIterator();
                return next();
            }
            return nodeIterator.next();
        }

    }

    private static class BackwardWay implements IWay {
        private IWay way;

        public BackwardWay(final IWay way) {
            this.way = way;
        }

        public Iterator<Node> iterator() {
            return way.descendingIterator();
        }

        @Override
        public int getType() {
            return way.getType();
        }

        @Override
        public String getName() {
            return way.getName();
        }

        @Override
        public Iterator<Node> descendingIterator() {
            return way.iterator();
        }
    }
}
