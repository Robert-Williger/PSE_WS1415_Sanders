package map;

import static org.junit.Assert.assertEquals;
import model.map.IPixelConverter;
import model.map.PixelConverter;

import org.junit.Before;
import org.junit.Test;

public class PixelConverterTest {

    private IPixelConverter converter;

    @Before
    public void setUp() {
        converter = new PixelConverter(1.5);
    }

    @Test
    public void testCoordConversion() {
        assertEquals(400, converter.getPixelDistance(150, 2));
    }

    @Test
    public void testPixelConversion() {
        assertEquals(150, converter.getCoordDistance(400, 2));
    }
}
