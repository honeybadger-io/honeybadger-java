package io.honeybadger.reporter.util;

import io.honeybadger.util.HBCollectionUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HBCollectionUtilsTest {
    @Test
    public void canParseEmptyCSV() {
        String csv = "";
        Collection<String> actual = HBCollectionUtils.parseNaiveCsvString(csv);
        assertTrue("Collection should be empty", actual.isEmpty());
    }

    @Test
    public void canParseNullCSV() {
        String csv = null;
        Collection<String> actual = HBCollectionUtils.parseNaiveCsvString(csv);
        assertTrue("Collection should be empty", actual.isEmpty());
    }

    @Test
    public void canParseCSVWithNoSpaces() {
        String csv = "cat,hat,dog,bog";
        Collection<String> actual = HBCollectionUtils.parseNaiveCsvString(csv);
        assertEquals(String.format("Collection should have 4 elements. Actual: %s", actual),
                4, actual.size());
        List<String> asList = new ArrayList<>(actual);
        assertEquals("cat", asList.get(0));
        assertEquals("hat", asList.get(1));
        assertEquals("dog", asList.get(2));
        assertEquals("bog", asList.get(3));
    }

    @Test
    public void canParseCSVWithSpaces() {
        String csv = "cat, hat , dog,  bog";
        Collection<String> actual = HBCollectionUtils.parseNaiveCsvString(csv);
        assertEquals(String.format("Collection should have 4 elements. Actual: %s", actual),
                4, actual.size());
        List<String> asList = new ArrayList<>(actual);
        assertEquals("cat", asList.get(0));
        assertEquals("hat", asList.get(1));
        assertEquals("dog", asList.get(2));
        assertEquals("bog", asList.get(3));
    }
}
