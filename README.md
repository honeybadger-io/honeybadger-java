honeybadger-jvm-client-v2
================

*Java Client to report exceptions to Honeybadger.io*

## Origin
Forked from <a href="https://github.com/styleseek/honeybadger-java">honeybadger-java</a> - 
thanks for doing the hard work for figuring out the API.

## Description
This is a library for sending errors that implement ```java.lang.Throwable``` on the JVM to the online error reporting service <a href="https://www.honeybadger.io/">Honeybadger</a>.

## Download / Maven Repository
You can find the library on <a href="http://search.maven.org/#browse%7C-1627719036">Maven Central</a> or you can always clone this github repository.

## Implementation
If you want to send all unhandled errors to Honeybadger and have them logged to slf4j via 
the error log level, you will need to set some system properties and add a single line 
to the thread in which you want to register the error handler.

System Properties:
 - honeybadger.api_key - set this to the (typically 8 character) API key displayed on your Honeybadger interface
 - honeybadger.excluded_sys_props - a comma delinated list of system property
   keys to exclude from being reported to Honeybadger. This allows you to prevent
   passwords and other sensitive information from being sent.
 - honeybadger.excluded_exception_classes - a comma delinated list of fully formed
   class names that will be excluded from error reporting.
 - JAVA_ENV / ENV - set this to configure the application's running environment

A typical stand-alone implementation may look like:

```java
import io.honeybadger.reporter.HoneybadgerUncaughtExceptionHandler;

public static void main(String argv[]) {
    HoneybadgerUncaughtExceptionHandler.registerAsUncaughtExceptionHandler();
    // The rest of the application goes here
}
```

A servlet based implemantion may look like:

In your web.xml file:
```xml
    <!-- Send all uncaught servlet exceptions and servlet request details to Honeybadger -->
    <filter>
        <filter-name>HoneybadgerFilter</filter-name>
        <filter-class>io.honeybadger.reporter.servlet.HoneybadgerFilter</filter-class>
        <init-param>
            <param-name>honeybadger.api_key</param-name>
            <param-value>API KEY GOES HERE</param-value>
        </init-param>
        <init-param>
            <param-name>honeybadger.excluded_sys_props</param-name>
            <param-value>bonecp.password,bonecp.username</param-value>
        </init-param>
        <init-param>
            <param-name>honeybadger.excluded_exception_classes</param-name>
            <param-value>org.apache.catalina.connector.ClientAbortException</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>HoneybadgerFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```

If you want to send exceptions to HoneyBadger without having to register an uncaught 
exception handler, you can create an instance of ```HonebadgerReporter``` and call 
the ```reportError(Throwable error)``` method directly.

## Testing
For the purpose of one off testing, you can use the CLI utility. Just execute it using
```./gradlew run``` and you can enter in your API key and message to be sent to
Honeybadger.

### Unit and Integration Tests
This library by requires access to the remote Honeybadger API, so in order to
run automated tests you will need to specify system properties to Java in order
configure the remote API key and other settings.

If you are running the tests from gradle, it is easy to specify system properties
using a gradle.properties file placed in the root of the project.

A sample gradle.properties file may look like:

```
systemProp.ENV = TEST
systemProp.honeybadger.api_key = 1efa3d71
```

With this in place you can run the tests from gradle by using the wrapped
version of gradle that is bundled with the project by:

```
./gradlew test
```

If you are executing your tests from an IDE like IntelliJ, you may need to
manually set the system variables as part of the test run configuration.

## System Properties

The following properties are available:

| System Property                        | Sample Value                                       | Required? | Default Value                                                      | Description                                                                                                                                                                                |
|----------------------------------------|----------------------------------------------------|-----------|--------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ENV                                    | production                                         | No        | development                                                        | Any string value. String sent to Honeybadger indicating running environment (eg development, test, staging, production, etc). This property can also be read from an environment variable. |
| honeybadger.api_key                    | 29facd41                                           | Yes       | N/A                                                                | The API key found in the settings tab in the Honeybadger UI.                                                                                                                               |
| honeybadger.url                        | https://alternative.host/v1/notices                | No        | https://api.honeybadger.io/v1/notices                              | URL to the Honeybadger API endpoint. You may want to access it without SSL in order to test with a proxy utility.                                                                          |
| http.proxyHost                         | localhost                                          | No        |                                                                    | Standard Java system property for specifying the host to proxy all HTTP traffic through.                                                                                                   |
| http.proxyPort                         | 8888                                               | No        |                                                                    | Standard Java system property for specifying the port to proxy all HTTP traffic through.                                                                                                   |
| honeybadger.excluded_exception_classes | org.apache.catalina.connector.ClientAbortException | No        |                                                                    | CSV of Java classes in which errors are never sent to Honeybadger. This is useful for errors that are bubbled up from underlying frameworks like Tomcat.                                   |
| honeybadger.excluded_sys_props         | bonecp.password,bonecp.username                    | No        | honeybadger.api_key,honeybadger.excluded_sys_props,honeybadger.url | CSV of Java system properties to exclude from being logged to Honeybadger. This is useful for excluding authentication information.                                                        |


## Honeybadger Resources
I found this <a href="https://www.honeybadger.io/pages/collector">page</a> very helpful when trying to understand what data the Honeybadger API accepts. In particular, the gist linked to from the page has a solid example of what to send to the API.
