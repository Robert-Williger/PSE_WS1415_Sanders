package adminTool;

import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

import adminTool.elements.IPointAccess;
import util.AddressableBinaryHeap;
import util.IAddressablePriorityQueue;
import util.IntList;

public class VisvalingamWhyatt {

    private final int threshold;

    public VisvalingamWhyatt(final int threshold) {
        this.threshold = threshold;
    }

    public IntList simplifyPolygon(final IPointAccess points, final PrimitiveIterator.OfInt element) {
        Item head = setupList(element);
        final int size = head.priority;
        IAddressablePriorityQueue<Item> queue = setupQueue(points, head, size);
        head = performSimplification(points, queue, head);

        return createReturnList(head, !queue.isEmpty() ? queue.size() : 0);
    }

    public IntList simplifyMultiline(final IPointAccess points, final PrimitiveIterator.OfInt element) {
        final Item head = setupList(element);
        final int size = head.priority;
        IAddressablePriorityQueue<Item> queue = setupQueue(points, head.next, size - 2);
        performSimplification(points, queue, null);

        return createReturnList(head, !queue.isEmpty() ? queue.size() + 2 : 2);
    }

    public IntList simplifyPolygon(final IPointAccess points, final int from, final int nodes) {
        Item head = setupList(from, nodes);
        IAddressablePriorityQueue<Item> queue = setupQueue(points, head, nodes);
        head = performSimplification(points, queue, head);

        return createReturnList(head, !queue.isEmpty() ? queue.size() : 0);
    }

    public IntList simplifyMultiline(final IPointAccess points, final int from, final int nodes) {
        Item head = setupList(from, nodes);
        IAddressablePriorityQueue<Item> queue = setupQueue(points, head.next, nodes - 2);
        performSimplification(points, queue, null);

        return createReturnList(head, !queue.isEmpty() ? queue.size() + 2 : 2);
    }

    private Item setupList(final PrimitiveIterator.OfInt element) {
        Item head = new Item(element.nextInt());
        Item last = head;

        int size = 1;
        while (element.hasNext()) {
            final Item current = new Item(element.nextInt());
            current.previous = last;
            last.next = current;

            last = current;
            ++size;
        }
        last.next = head;
        head.previous = last;

        head.priority = size;
        return head;
    }

    private Item setupList(final int from, final int nodes) {
        Item head = new Item(from);
        Item last = head;

        for (int i = from + 1; i < from + nodes; ++i) {
            final Item current = new Item(i);
            current.previous = last;
            last.next = current;

            last = current;
        }
        last.next = head;
        head.previous = last;

        return head;
    }

    private IAddressablePriorityQueue<Item> setupQueue(final IPointAccess points, final Item from, final int nodes) {
        final IAddressablePriorityQueue<Item> queue = new AddressableBinaryHeap<>();

        List<Item> items = new ArrayList<>(nodes);
        List<Integer> priorities = new ArrayList<>(nodes);

        Item current = from;

        for (int i = 0; i < nodes; i++) {
            updatePriority(points, current);
            items.add(current);
            priorities.add(current.priority);
            current = current.next;
        }

        queue.addAll(items, priorities);

        return queue;
    }

    private Item performSimplification(final IPointAccess points, final IAddressablePriorityQueue<Item> queue,
            final Item head) {
        Item currentHead = head;
        while (!queue.isEmpty()) {
            final Item item = queue.deleteMin();

            if (item == currentHead) {
                currentHead = currentHead.next;
            }
            if (item.priority < threshold) {
                Item next = item.next;
                Item previous = item.previous;

                next.previous = previous;
                previous.next = next;

                updatePriority(points, next);
                queue.changeKey(next, next.priority);

                updatePriority(points, previous);
                queue.changeKey(previous, previous.priority);

            } else {
                queue.insert(item, item.priority);
                return currentHead;
            }
        }

        return null;
    }

    private IntList createReturnList(final Item head, final int nodes) {
        IntList ret = new IntList(nodes);

        Item current = head;
        for (int i = 0; i < nodes; i++) {
            ret.add(current.index);
            current = current.next;
        }

        return ret;
    }

    private void updatePriority(final IPointAccess points, final Item item) {
        item.priority = getArea(points, item.previous.index, item.index, item.next.index);
    }

    private int getArea(final IPointAccess points, final int previous, final int current, final int next) {
        final int lastX = points.getX(previous);
        final int lastY = points.getY(previous);

        final int currentX = points.getX(current);
        final int currentY = points.getY(current);

        final int nextX = points.getX(next);
        final int nextY = points.getY(next);

        // a = last, b = current, c = next

        return (int) (Math.abs((lastX - nextX) * (currentY - lastY) - (lastX - currentX) * (nextY - lastY)) * 0.5);
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
