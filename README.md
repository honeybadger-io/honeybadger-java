honeybadger-jvm-client-v2
================

*Java Client to report exceptions to Honeybadger.io*

### Origin
Forked from <a href="https://github.com/styleseek/honeybadger-java">honeybadger-java</a> - 
thanks for doing the hard work for figuring out the API.

### Description
This is a library for sending errors that implement ```java.lang.Throwable``` on the JVM to the online error reporting service <a href="https://www.honeybadger.io/">Honeybadger</a>.

### Download
You can find the library on <a href="http://search.maven.org/#browse%7C-1627719036">Maven Central</a> or you can always clone this github repository.

### Implementation
If you want to send all unhandled errors to Honeybadger and have them logged to slf4j via 
the error log level, you will need to set some system properties and add a single line 
to the thread in which you want to register the error handler.

System Properties:
 - honeybadger.api_key - set this to the (typically 8 character) API key displayed on your Honeybadger interface
 - honeybadger.excluded_sys_props - a comma delinated list of system property
   keys to exclude from being reported to Honeybadger. This allows you to prevent
   passwords and other sensitive information from being sent.
 - JAVA_ENV / ENV - set this to configure the application's running environment

A typical implementation may look like:

```java
import org.dekobon.honeybadger.HoneybadgerUncaughtExceptionHandler;

...

public static void main(String argv[]) {
    HoneybadgerUncaughtExceptionHandler.registerAsUncaughtExceptionHandler();
}
```

If you want to send exceptions to HoneyBadger without having to register an uncaught 
exception handler, you can create an instance of ```HonebadgerReporter``` and call 
the ```reportError(Throwable error)``` method directly.

### Testing
For the purpose of testing, you can use the CLI utility. Just execute it using
```gradle run``` and you can enter in your API key and message to be sent to
Honeybadger.
