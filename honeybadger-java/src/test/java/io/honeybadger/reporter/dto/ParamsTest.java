package io.honeybadger.reporter.dto;

import io.honeybadger.reporter.config.ConfigContext;
import io.honeybadger.reporter.config.StandardConfigContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParamsTest {
    @Test
    public void willExcludeUnwantedParams() {
        ConfigContext config = new StandardConfigContext();
        config.getExcludedParams().add("cc_no");
        config.getExcludedParams().add("auth_token");
        Params params = new Params(config.getExcludedParams());

        params.put("user", "duck");
        params.put("cc_no", "933200234432230232323");
        params.put("action", "quack");

        assertEquals("We expect one value to be excluded so the size should be 2",
                     2, params.size());
        assertTrue("We expect the user key to be present", params.containsKey("user"));
        assertTrue("We expect the action key to be present", params.containsKey("action"));
        assertFalse("We expect the cc_no key to not be present", params.containsKey("cc_no"));
    }
}
