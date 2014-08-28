honeybadger-jvm-client-v2
================

*Java Client to report exceptions to Honeybadger.io*

Forked from <a href="https://github.com/styleseek/honeybadger-java">honeybadger-java</a> - 
thanks for doing the hard work for figuring out the API.

If you want to send all unhandled errors to Honeybadger and have them logged to slf4j via 
the error log level, you will need to set some system properties and add a single line 
to the thread in which you want to register the error handler.

System Properties:
 - honeybadger.api_key - set this to the (typically 8 character) API key displayed on your Honeybadger interface
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

For the purpose of testing, you can use the CLI utility. Just execute it using
```gradle run``` and you can enter in your API key and message to be sent to
Honeybadger.
