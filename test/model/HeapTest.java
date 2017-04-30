package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import util.AddressableBinaryHeap;

public class HeapTest {
    private AddressableBinaryHeap<Integer> queue;

    @Before
    public void setUp() {
        queue = new AddressableBinaryHeap<>();
    }

    @Test
    public void testAscendingInput() {
        boolean failed = false;
        for (int i = 0; i < 20; i++) {
            queue.insert(i, i);
        }

        for (int i = 0; i < queue.size(); i++) {
            if (i != queue.deleteMin()) {
                failed = true;
            }
        }
        assertFalse(failed);
    }

    @Test
    public void testDecendingInput() {
        boolean failed = false;
        for (int i = 0; i < 20; i++) {
            queue.insert(19 - i, 19 - i);
        }

        for (int i = 0; i < queue.size(); i++) {
            if (i != queue.deleteMin()) {
                failed = true;
            }
        }
        assertFalse(failed);
    }

    @Test
    public void testChangeKey() {
        queue.insert(1, 1);
        queue.insert(2, 2);
        queue.changeKey(1, 100);

        final int ret = queue.deleteMin();
        assertEquals(2, ret);
    }

    @Test
    public void testSize() {
        final int n = 20;
        for (int i = 0; i < n; i++) {
            queue.insert(i, i);
        }

        assertEquals(n, queue.size());
    }

    @Test
    public void testDelete() {
        boolean failed = false;
        for (int i = 0; i < 20; i++) {
            queue.insert(i, i);
        }

        queue.remove(10);
        queue.remove(16);

        for (int i = 0; i < queue.size(); i++) {
            if (i == 16 || i == 10) {
            } else {
                if (i != queue.deleteMin()) {
                    failed = true;
                }
            }

        }
        assertFalse(failed);
    }

    @Test
    public void testContains() {
        queue.insert(12, 1);
        assertTrue(queue.contains(12));
    }

    @Test
    public void testAddAll() {
        boolean failed = false;
        final List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        queue.addAll(list, list);

        for (int i = 0; i < queue.size(); i++) {

            if (i != queue.deleteMin()) {
                failed = true;
            }
        }
        assertFalse(failed);
    }

    @Test
    public void testMerge() {
        boolean failed = false;
        final AddressableBinaryHeap<Integer> queue2 = new AddressableBinaryHeap<>();

        for (int i = 0; i < 50; i++) {
            queue.insert(i, i);
        }

        for (int i = 50; i < 100; i++) {
            queue2.insert(i, i);
        }

        queue.merge(queue2);

        for (int i = 0; i < queue.size(); i++) {

            if (i != queue.deleteMin()) {
                failed = true;
            }
        }
        assertFalse(failed);
    }

}
