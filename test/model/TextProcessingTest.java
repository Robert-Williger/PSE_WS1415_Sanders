package model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.elements.IStreet;
import model.elements.Street;
import model.elements.StreetNode;

import org.junit.Before;
import org.junit.Test;

public class TextProcessingTest {

    private static final int length = 3;

    private TextProcessor tp;

    private StreetNode sn;

    @Before
    public void setUp() {

        // Erstellt fuer den richtigen Eintrag der Hashtabelle einen passenden
        // Node

        final IStreet iStreet = new Street(new int[0], 0, "Teststrasse", 0);
        sn = new StreetNode(0, iStreet);

        final HashMap<String, StreetNode> hm = new HashMap<String, StreetNode>();
        hm.put("Te4tst2agse", null);
        hm.put("Teststrahse", null);
        hm.put("Test", null);
        hm.put("EIDAKDSLENCP", null);
        hm.put("Teststrasse", sn);

        tp = new TextProcessor(hm, length);
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

        final StreetNode testNode = tp.parse("Teststrasse");
        assertEquals(sn, testNode);
    }

    @Test
    public void testNormalize() {

        String name = "Wssßäaäüüöössw. 31";
        name = TextProcessor.normalize(name);
        final String normName = "wssssaeaaeueueoeoessw. 31";
        assertEquals(normName, name);

    }

}
