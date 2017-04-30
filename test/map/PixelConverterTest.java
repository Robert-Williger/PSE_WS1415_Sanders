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
        converter = new PixelConverter(2);
    }

    @Test
    public void testCoordConversion() {
        assertEquals(75, converter.getPixelDistance(150, 1));
    }

    @Test
    public void testPixelConversion() {
        assertEquals(150, converter.getCoordDistance(75, 1));
    }
}
