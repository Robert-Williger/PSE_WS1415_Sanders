package adminTool;

import java.util.ArrayList;
import java.util.List;

import adminTool.elements.MultiElement;
import util.AddressableBinaryHeap;
import util.IAddressablePriorityQueue;
import model.map.IPixelConverter;

public class VisvalingamWhyatt {

    private final IPixelConverter converter;
    private final int threshHold;
    private final PointAccess points;

    public VisvalingamWhyatt(final IPixelConverter converter, final PointAccess points, final int threshHold) {
        this.converter = converter;
        this.points = points;
        this.threshHold = threshHold;
    }

    public int[] simplifyPolygon(final MultiElement element, final int zoom) {
        Item head = setupList(element);
        IAddressablePriorityQueue<Item> queue = setupQueue(head, head.previous.index + 1, zoom);
        head = performSimplification(queue, head, zoom);

        return createReturnArray(head, !queue.isEmpty() ? queue.size() : 0);
    }

    public int[] simplifyMultiline(final MultiElement element, final int zoom) {
        Item head = setupList(element);
        IAddressablePriorityQueue<Item> queue = setupQueue(head.next, head.previous.index - 1, zoom);
        performSimplification(queue, null, zoom);

        return createReturnArray(head, !queue.isEmpty() ? queue.size() + 2 : 2);
    }

    private Item setupList(final MultiElement element) {
        Item head = new Item(element.getNode(0));
        Item last = head;

        for (int i = 1; i < element.size(); ++i) {
            final Item current = new Item(element.getNode(i));
            current.previous = last;
            last.next = current;

            last = current;
        }
        last.next = head;
        head.previous = last;

        return head;
    }

    private IAddressablePriorityQueue<Item> setupQueue(final Item from, final int nodes, final int zoom) {
        final IAddressablePriorityQueue<Item> queue = new AddressableBinaryHeap<>();

        List<Item> items = new ArrayList<>(nodes);
        List<Integer> priorities = new ArrayList<>(nodes);

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
        item.priority = getArea(item.previous.index, item.index, item.next.index, zoom);
    }

    private int getArea(final int previous, final int current, final int next, final int zoom) {
        final int lastX = converter.getPixelDistance(points.getX(previous), zoom);
        final int lastY = converter.getPixelDistance(points.getY(previous), zoom);

        final int currentX = converter.getPixelDistance(points.getX(current), zoom);
        final int currentY = converter.getPixelDistance(points.getY(current), zoom);

        final int nextX = converter.getPixelDistance(points.getX(next), zoom);
        final int nextY = converter.getPixelDistance(points.getY(next), zoom);

        // a = last, b = current, c = next

        return (int) (Math.abs((lastX - nextX) * (currentY - lastY) - (lastX - currentX) * (nextY - lastY)) * 0.5 + 0.5);
    }

    private static class Item {
        private int index;
        private int priority;
        private Item previous;
        private Item next;

        public Item(final int index) {
            this.index = index;
        }
    }
}
