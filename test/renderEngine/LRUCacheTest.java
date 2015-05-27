package renderEngine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;

import model.renderEngine.LRUCache;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LRUCacheTest {

    private LRUCache cache;
    private int capacity;
    private static Image image;

    @BeforeClass
    public static void setUpClass() {
        image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
    }

    @Before
    public void setUp() {
        final Queue<Image> freeList = new LinkedList<Image>();
        capacity = 10;
        for (int i = 0; i < capacity; i++) {
            freeList.add(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB));
        }

        cache = new LRUCache(capacity, false, freeList);
    }

    @Test
    public void testContains() {
        assertFalse(cache.contains(5));
        cache.put(5, image);
        assertTrue(cache.contains(5));
    }

    @Test
    public void testGetImage() {
        assertNull(cache.get(8));
        cache.put(8, image);
        assertEquals(cache.get(8), image);
    }

    @Test
    public void testLRUFunctionality() {
        final BufferedImage image2 = new BufferedImage(11, 11, BufferedImage.TYPE_INT_ARGB);
        cache.put(0, image2);
        assertEquals(image2, cache.get(0));

        for (int i = 1; i < capacity; i++) {
            cache.put(i, image);
        }

        for (int i = 1; i < capacity; i++) {
            cache.get(i);
        }

        assertTrue(cache.contains(0));
        cache.put(100, image);
        assertNull(cache.get(0));
    }

    @Test
    public void testReset() {
        assertFalse(cache.contains(5));
        cache.put(5, image);
        assertTrue(cache.contains(5));
        cache.reset();
        assertFalse(cache.contains(5));
    }

    @Test
    public void testResize() {
        cache.setSize(2);
        cache.put(0, image);
        cache.put(1, image);
        assertTrue(cache.contains(0));
        cache.put(2, image);
        assertFalse(cache.contains(0));
    }
}
