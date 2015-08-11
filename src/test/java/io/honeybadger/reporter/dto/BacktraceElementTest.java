package io.honeybadger.reporter.dto;

import io.honeybadger.reporter.NoticeReporter;
import org.junit.Test;

import javax.annotation.concurrent.NotThreadSafe;

import static io.honeybadger.reporter.dto.BacktraceElement.calculateContext;
import static org.junit.Assert.assertEquals;

@NotThreadSafe
public class BacktraceElementTest {
    private final static String METHOD_NAME = "io.honeybadger.dog.food.ClassName:methodName";
    private final static String APP_PACKAGE = "io.honeybadger";

    @Test
    public void canCalculateContextWhenNoAppPackageIsSet() {
        System.setProperty(NoticeReporter.APPLICATION_PACKAGE_PROP_KEY, "");
        BacktraceElement.Context context = calculateContext(METHOD_NAME);
        assertEquals("Context should be ALL because we haven't set a package",
                     BacktraceElement.Context.ALL, context);
    }

    @Test
    public void canCalculateContextFromMatchingPackage() {
        System.setProperty(NoticeReporter.APPLICATION_PACKAGE_PROP_KEY, APP_PACKAGE);
        BacktraceElement.Context context = calculateContext(METHOD_NAME);
        assertEquals("Context should be APP because we have set a package and it matches",
                BacktraceElement.Context.APP, context);
    }

    @Test
    public void canCalculateContextFromNotMatchingPackage() {
        System.setProperty(NoticeReporter.APPLICATION_PACKAGE_PROP_KEY, APP_PACKAGE);
        BacktraceElement.Context context = calculateContext("com.derp.herp.ClassName.methodName");
        assertEquals("Context should be ALL because we have set a package and it doesn't match",
                BacktraceElement.Context.ALL, context);
    }

    @Test
    public void canCalculateContextFromNullMethod() {
        System.setProperty(NoticeReporter.APPLICATION_PACKAGE_PROP_KEY, APP_PACKAGE);
        BacktraceElement.Context context = calculateContext(null);
        assertEquals("Context should be ALL because we have a null method",
                BacktraceElement.Context.ALL, context);
    }

    @Test
    public void canCalculateContextFromEmptyMethod() {
        System.setProperty(NoticeReporter.APPLICATION_PACKAGE_PROP_KEY, APP_PACKAGE);
        BacktraceElement.Context context = calculateContext("");
        assertEquals("Context should be ALL because we have an empty method",
                BacktraceElement.Context.ALL, context);
    }
}
