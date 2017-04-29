package model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import model.targets.AddressPoint;

public class TextProcessingTest {

    private TextProcessor tp;

    @Before
    public void setUp() {
        tp = new TextProcessor();
    }

    @Test
    public void testSuggest() {

        final List<String> list = tp.suggest("Teststrasse");

        final List<String> comList = new ArrayList<>();
        comList.add("Teststrasse");
        comList.add("Teststrahse");
        comList.add("Te4tst2agse");

        assertEquals("Passende Liste: ", comList, list);

    }

    @Test
    public void testSuggestShort() {
        final List<String> list = tp.suggest("EIDA");
        assertEquals("EIDAKDSLENCP", list.get(0));
    }

    @Test
    public void testParse() {

        final AddressPoint testNode = tp.parse("Teststrasse");
        assertNotNull(testNode);
    }

    @Test
    public void testNormalize() {
        String name = "Wssßäaäüüöössw. 31";
        name = TextProcessor.normalize(name);
        final String normName = "wssssaeaaeueueoeoessw. 31";
        assertEquals(normName, name);

    }

}
