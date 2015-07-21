package io.honeybadger.reporter.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CausesTest {

    @Test
    public void noCauseStoresNothing() {
        Throwable e = new RuntimeException("No cause");
        Causes causes = new Causes(e);

        assertTrue("There should be no causes stored because the exception had no chained exceptions",
                   causes.isEmpty());
    }

    @Test
    public void oneCauseStored() {
        Throwable cause = new RuntimeException("Cause");
        Throwable e = new RuntimeException("Highest level", cause);

        Causes causes = new Causes(e);

        assertEquals("There should only be a single cause",
                1, causes.size());
        assertEquals("The cause class should be stored",
                     cause.getMessage(), causes.get(0).message);
    }

    @Test
    public void lastItemInCausesIsTheRootCause() {
        Throwable cause4 = new RuntimeException("Cause 4");
        Throwable cause3 = new RuntimeException("Cause 3", cause4);
        Throwable cause2 = new RuntimeException("Cause 2", cause3);
        Throwable cause1 = new RuntimeException("Cause 1", cause2);

        Throwable e = new RuntimeException("Highest level", cause1);

        Causes causes = new Causes(e);

        assertEquals("There should be 4 causes chained",
                4, causes.size());
        assertEquals("The root cause class should be stored last",
                cause1.getMessage(), causes.get(3).message);
        assertEquals("The second cause class should be stored second to last",
                cause2.getMessage(), causes.get(2).message);
        assertEquals("The third cause class should be stored third to last",
                cause3.getMessage(), causes.get(1).message);
        assertEquals("The fourth cause class should be stored first",
                cause4.getMessage(), causes.get(0).message);

    }
}
