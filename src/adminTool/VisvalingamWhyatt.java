package adminTool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.elements.Node;
import model.map.IPixelConverter;
import model.routing.AddressableBinaryHeap;
import model.routing.IAddressablePriorityQueue;

public class VisvalingamWhyatt {

    private final IPixelConverter converter;
    private final int threshHold;

    public VisvalingamWhyatt(final IPixelConverter converter, final int threshHold) {
        this.converter = converter;
        this.threshHold = threshHold;
    }

    public int[] simplifyPolygon(final Iterator<Node> nodes, final int zoom) {
        Item head = setupList(nodes);
        IAddressablePriorityQueue<Item> queue = setupQueue(head, head.previous.index + 1, zoom);
        head = performSimplification(queue, head, zoom);

        return createReturnArray(head, !queue.isEmpty() ? queue.size() : 0);
    }

    public int[] simplifyMultiline(final Iterator<Node> nodes, final int zoom) {

        Item head = setupList(nodes);
        IAddressablePriorityQueue<Item> queue = setupQueue(head.next, head.previous.index - 1, zoom);
        performSimplification(queue, null, zoom);

        return createReturnArray(head, !queue.isEmpty() ? queue.size() + 2 : 2);
    }

    private Item setupList(final Iterator<Node> nodes) {
        Item head = new Item(nodes.next(), 0);
        Item last = head;

        int index = 0;
        while (nodes.hasNext()) {
            final Item current = new Item(nodes.next(), ++index);
            current.previous = last;
            last.next = current;

            last = current;
        }
        last.next = head;
        head.previous = last;

        return head;
    }

    private IAddressablePriorityQueue<Item> setupQueue(final Item from, final int nodes, final int zoom) {
        final IAddressablePriorityQueue<Item> queue = new AddressableBinaryHeap<Item>();

        List<Item> items = new ArrayList<Item>(nodes);
        List<Integer> priorities = new ArrayList<Integer>(nodes);

        Item current = from;

        for (int i = 0; i < nodes; i++) {
            updatePriority(current, zoom);
            items.add(current);
            priorities.add(current.priority);
            current = current.next;
        }

        queue.addAll(items, priorities);

        return queue;
    }

    private Item performSimplification(final IAddressablePriorityQueue<Item> queue, final Item head, final int zoom) {
        Item currentHead = head;
        while (!queue.isEmpty()) {
            final Item item = queue.deleteMin();

            if (item == currentHead) {
                currentHead = currentHead.next;
            }
            if (item.priority < threshHold) {
                Item next = item.next;
                Item previous = item.previous;

                next.previous = previous;
                previous.next = next;

                updatePriority(next, zoom);
                queue.changeKey(next, next.priority);

                updatePriority(previous, zoom);
                queue.changeKey(previous, previous.priority);

            } else {
                queue.insert(item, item.priority);
                return currentHead;
            }
        }

        return null;
    }

    private int[] createReturnArray(final Item head, final int nodes) {
        int[] ret = new int[nodes];

        Item current = head;
        for (int i = 0; i < nodes; i++) {
            ret[i] = current.index;
            current = current.next;
        }

        return ret;
    }

    private void updatePriority(final Item item, final int zoom) {
        item.priority = getArea(item.previous.node, item.node, item.next.node, zoom);
    }

    private int getArea(final Node previous, final Node current, final Node next, final int zoom) {
        final int lastX = converter.getPixelDistance(previous.getX(), zoom);
        final int lastY = converter.getPixelDistance(previous.getY(), zoom);

        final int currentX = converter.getPixelDistance(current.getX(), zoom);
        final int currentY = converter.getPixelDistance(current.getY(), zoom);

        final int nextX = converter.getPixelDistance(next.getX(), zoom);
        final int nextY = converter.getPixelDistance(next.getY(), zoom);

        // a = last, b = current, c = next

        return (int) (Math.abs((lastX - nextX) * (currentY - lastY) - (lastX - currentX) * (nextY - lastY)) * 0.5 + 0.5);
    }

    private static class Item {
        private Node node;
        private int index;
        private int priority;
        private Item previous;
        private Item next;

        public Item(final Node node, final int index) {
            this.node = node;
            this.index = index;
        }
    }
}
