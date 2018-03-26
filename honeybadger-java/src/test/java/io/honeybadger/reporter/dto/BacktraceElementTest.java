package io.honeybadger.reporter.dto;

import io.honeybadger.reporter.config.ConfigContext;
import org.junit.Test;

import javax.annotation.concurrent.NotThreadSafe;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@NotThreadSafe
public class BacktraceElementTest {
    private final static String METHOD_NAME = "io.honeybadger.dog.food.ClassName:methodName";
    private final static String APP_PACKAGE = "io.honeybadger";

    @Test
    public void canCalculateContextWhenNoAppPackageIsSet() {
        ConfigContext config = mock(ConfigContext.class);
        when(config.getApplicationPackage()).thenReturn("");

        BacktraceElement instance = new BacktraceElement(config, null, null, null);
        BacktraceElement.Context context = instance.calculateContext(METHOD_NAME);
        assertEquals("Context should be ALL because we haven't set a package",
                     BacktraceElement.Context.ALL, context);
    }

    @Test
    public void canCalculateContextFromMatchingPackage() {
        ConfigContext config = mock(ConfigContext.class);
        when(config.getApplicationPackage()).thenReturn(APP_PACKAGE);

        BacktraceElement instance = new BacktraceElement(config, null, null, null);
        BacktraceElement.Context context = instance.calculateContext(METHOD_NAME);
        assertEquals("Context should be APP because we have set a package and it matches",
                BacktraceElement.Context.APP, context);
    }

    @Test
    public void canCalculateContextFromNotMatchingPackage() {
        ConfigContext config = mock(ConfigContext.class);
        when(config.getApplicationPackage()).thenReturn(APP_PACKAGE);

        BacktraceElement instance = new BacktraceElement(config, null, null, null);
        BacktraceElement.Context context = instance.calculateContext("com.derp.herp.ClassName.methodName");
        assertEquals("Context should be ALL because we have set a package and it doesn't match",
                BacktraceElement.Context.ALL, context);
    }

    @Test
    public void canCalculateContextFromNullMethod() {
        ConfigContext config = mock(ConfigContext.class);
        when(config.getApplicationPackage()).thenReturn(APP_PACKAGE);

        BacktraceElement instance = new BacktraceElement(config, null, null, null);
        BacktraceElement.Context context = instance.calculateContext(null);
        assertEquals("Context should be ALL because we have a null method",
                BacktraceElement.Context.ALL, context);
    }

    @Test
    public void canCalculateContextFromEmptyMethod() {
        ConfigContext config = mock(ConfigContext.class);
        when(config.getApplicationPackage()).thenReturn(APP_PACKAGE);

        BacktraceElement instance = new BacktraceElement(config, null, null, null);
        BacktraceElement.Context context = instance.calculateContext("");
        assertEquals("Context should be ALL because we have an empty method",
                BacktraceElement.Context.ALL, context);
    }
}
