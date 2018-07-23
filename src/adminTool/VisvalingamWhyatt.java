package adminTool;

import java.util.ArrayList;
import java.util.List;
import util.AddressableBinaryHeap;
import util.IAddressablePriorityQueue;
import util.IntList;

public class VisvalingamWhyatt {

    private final int threshold;

    public VisvalingamWhyatt(final int threshold) {
        this.threshold = threshold;
    }

    public IntList simplifyPolygon(final IElement element) {
        Item head = setupList(element);
        IAddressablePriorityQueue<Item> queue = setupQueue(element, head, element.size());
        head = performSimplification(element, queue, head);

        return createReturnList(head, !queue.isEmpty() ? queue.size() : 0);
    }

    public IntList simplifyMultiline(final IElement element) {
        final Item head = setupList(element);
        IAddressablePriorityQueue<Item> queue = setupQueue(element, head.next, element.size() - 2);
        performSimplification(element, queue, null);

        return createReturnList(head, !queue.isEmpty() ? queue.size() + 2 : 2);
    }

    private Item setupList(final IElement element) {
        Item head = new Item(0);
        Item last = head;

        for (int i = 1; i < element.size(); ++i) {
            final Item current = new Item(i);
            current.previous = last;
            last.next = current;

            last = current;
        }
        last.next = head;
        head.previous = last;

        return head;
    }

    private IAddressablePriorityQueue<Item> setupQueue(final IElement element, final Item from, final int nodes) {
        final IAddressablePriorityQueue<Item> queue = new AddressableBinaryHeap<>();

        List<Item> items = new ArrayList<>(nodes);
        List<Double> priorities = new ArrayList<>(nodes);

        Item current = from;

        for (int i = 0; i < nodes; i++) {
            updatePriority(element, current);
            items.add(current);
            priorities.add(current.priority);
            current = current.next;
        }

        queue.addAll(items, priorities);

        return queue;
    }

    private Item performSimplification(final IElement element, final IAddressablePriorityQueue<Item> queue,
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

                updatePriority(element, next);
                queue.changeKey(next, next.priority);

                updatePriority(element, previous);
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

    private void updatePriority(final IElement element, final Item item) {
        item.priority = getArea(element, item.previous.index, item.index, item.next.index);
    }

    private double getArea(final IElement element, final int previous, final int current, final int next) {
        final double lastX = element.getX(previous);
        final double lastY = element.getY(previous);

        final double currentX = element.getX(current);
        final double currentY = element.getY(current);

        final double nextX = element.getX(next);
        final double nextY = element.getY(next);

        // a = last, b = current, c = next

        return (Math.abs((lastX - nextX) * (currentY - lastY) - (lastX - currentX) * (nextY - lastY)) * 0.5);
    }

    private static class Item {
        private int index;
        private double priority;
        private Item previous;
        private Item next;

        public Item(final int index) {
            this.index = index;
        }
    }
}
