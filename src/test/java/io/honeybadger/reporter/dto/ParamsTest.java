package io.honeybadger.reporter.dto;

import org.junit.Test;

import static io.honeybadger.reporter.NoticeReporter.HONEYBADGER_EXCLUDED_PARAMS_SYS_PROP_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParamsTest {
    @Test
    public void willExcludeUnwantedParams() {
        System.setProperty(HONEYBADGER_EXCLUDED_PARAMS_SYS_PROP_KEY,
                "cc_no,auth_token");
        Params params = new Params();

        try {
            params.put("user", "duck");
            params.put("cc_no", "933200234432230232323");
            params.put("action", "quack");
        } finally {
            System.clearProperty(HONEYBADGER_EXCLUDED_PARAMS_SYS_PROP_KEY);
        }

        assertEquals("We expect one value to be excluded so the size should be 2",
                     2, params.size());
        assertTrue("We expect the user key to be present", params.containsKey("user"));
        assertTrue("We expect the action key to be present", params.containsKey("action"));
        assertFalse("We expect the cc_no key to not be present", params.containsKey("cc_no"));
    }
}
