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

    private LRUCache<Long, Image> cache;
    private int capacity;
    private static Image image;
    private long imageId;

    @BeforeClass
    public static void setUpClass() {
        image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
    }

    @Before
    public void setUp() {
        final Queue<Image> freeList = new LinkedList<>();
        capacity = 10;
        imageId = 0;
        for (int i = 0; i < capacity; i++) {
            freeList.add(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB));
        }

        cache = new LRUCache<>(capacity, false, freeList);
    }

    @Test
    public void testContains() {
        assertFalse(cache.contains(imageId));
        cache.put(imageId, image);
        assertTrue(cache.contains(imageId));
    }

    @Test
    public void testGetImage() {
        assertNull(cache.get(imageId));
        cache.put(imageId, image);
        assertEquals(cache.get(imageId), image);
    }

    @Test
    public void testLRUFunctionality() {
        final BufferedImage image2 = new BufferedImage(11, 11, BufferedImage.TYPE_INT_ARGB);
        cache.put(imageId, image2);
        assertEquals(image2, cache.get(imageId));

        for (long i = 1; i < capacity; i++) {
            cache.put(i, image);
        }

        for (long i = 1; i < capacity; i++) {
            cache.get(i);
        }

        assertTrue(cache.contains(imageId));
        cache.put(imageId + 100, image);
        assertNull(cache.get(imageId));
    }

    @Test
    public void testReset() {
        assertFalse(cache.contains(imageId));
        cache.put(imageId, image);
        assertTrue(cache.contains(imageId));
        cache.clear();
        assertFalse(cache.contains(imageId));
    }

    @Test
    public void testResize() {
        cache.setSize(2);
        cache.put(imageId, image);
        cache.put(imageId + 1, image);
        assertTrue(cache.contains(imageId));
        cache.put(imageId + 2, image);
        assertFalse(cache.contains(imageId));
    }
}
