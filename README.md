# Honeybadger for Java
 
[![Build Status](https://travis-ci.org/honeybadger-io/honeybadger-java.svg)](https://travis-ci.org/honeybadger-io/honeybadger-java)

Java client to report exceptions to the :zap: [Honeybadger.io error notification
service](https://www.honeybadger.io/). Receive instant notification of
exceptions and errors in your Java applications.

## Description
This is a library for sending errors that implement ```java.lang.Throwable``` on the JVM to the online error reporting service <a href="https://www.honeybadger.io/">Honeybadger</a>.

## Download / Maven Repository
You can find the library on <a href="https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.honeybadger%22">Maven Central</a> or you can always clone this github repository.

## Stand-alone Usage
If you want to send all unhandled errors to Honeybadger and have them logged to slf4j via 
the error log level, you will need to set some system properties and add a single line 
to the thread in which you want to register the error handler.

A typical stand-alone implementation may look like:

```java
import io.honeybadger.reporter.HoneybadgerUncaughtExceptionHandler;

public static void main(String argv[]) {
    HoneybadgerUncaughtExceptionHandler.registerAsUncaughtExceptionHandler();
    // The rest of the application goes here
}
```

## Servlet Usage
A servlet based implementation may look like:

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
        <!-- By default this is true. Toggle to false if you don't want users to be able
             to send feedback. -->
        <init-param>
            <param-name>honeybadger.display_feedback_form</param-name>
            <param-value>false</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>HoneybadgerFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```

## Play Framework Usage
This library has been tested against Play 2.4.2. You can add Honeybadger as an error
handler by adding the library to your SBT build file:
 
```
libraryDependencies += "io.honeybadger" % "honeybadger-java" % "<<VERSION NUMBER>>"
```
 
And then by adding the following lines to your conf/application.conf file:

```
honeybadger.api_key = <<API KEY>>
# You can add any of the Honeybadger configuration parameters here directly
# honeybadger.excluded_exception_classes = com.myorg.AnnoyingException 
play.http.errorHandler = io.honeybadger.reporter.play.HoneybadgerErrorHandler
```

This will allow the library to wrap the default error handler implementation and
pass around Honeybadger error ids instead of the default Play error ids.

## API Only Usage
If you want to send exceptions to HoneyBadger without having to register an uncaught 
exception handler, you can create an instance of ```HonebadgerReporter``` and call 
the ```reportError(Throwable error)``` method directly.

This is an example of calling how you might configure the Honeybadger
library and use it to send an error to the API programmatically.

```java
package com.myapp;

import io.honeybadger.reporter.HoneybadgerReporter;
import io.honeybadger.reporter.NoticeReporter;
import io.honeybadger.reporter.config.StandardConfigContext;

public class ApiUsage {
    public static void main(String[] argv) {
        StandardConfigContext config = new StandardConfigContext();
        config.setApiKey("ab3fg93xs")
              .setEnvironment("staging")
              .setApplicationPackage("com.myapp");
        
        NoticeReporter reporter = new HoneybadgerReporter();
        Throwable t = new RuntimeException("I'm a custom error");
        reporter.reportError(t);
    }
}
```

## Runtime Dependencies
All dependencies needed for running are included in the distributed JAR with one
exception - slf4j-api. We expect that you are using some logging library and that
you have imported the sl4j-api in order to provide a common interface for the 
logger to imported libraries. If you are using Maven, Gralde or Ivy, this should
happen automatically for you. If you are using Ant, you will need to import
the slf4j-api jar and an sl4j implementation.

## Testing
For the purpose of one off testing, you can use the CLI utility. Just execute it using
```./gradlew run``` and you can enter in your API key and message to be sent to
Honeybadger.

### Unit and Integration Tests
This library by requires access to the remote Honeybadger API, so in order to
run automated tests you will need to specify system properties to Java in order
configure the remote API key and other settings.

If you are running the tests from gradle, it is easy to specify system properties
using a gradle.properties file placed in the root of the project or in your 
$HOME/.gradle directory.

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

For developers pushing to Maven repositories, you will need to specify
the location of your signing keys in gradle.properties. You also shouldn't
put the file in the project root, but rather store it in your .gradle directory
within your home directory. Properties for signing look like:

```
signing.keyId=345A20CE
signing.password=J6N0phB*f3aRH4bZ
signing.secretKeyRingFile=/home/user/.gnupg/secring.gpg

ossrhUsername=user
ossrhPassword=AFbz3BjdE4Q9g2E&
```

## Advanced Configuration

The following properties are available:

```

ENV
-------------
Sample Value: production
Required?: No
Default Value: development
Description: Any string value. String sent to Honeybadger indicating running 
             environment (eg development, test, staging, production, etc). This 
             property can also be read from an environment variable. 

JAVA_ENV
-------------
Sample Value: production
Required?: No
Default Value: development
Description: Any string value. String sent to Honeybadger indicating running 
             environment (eg development, test, staging, production, etc). This 
             property can also be read from an environment variable. This is the
             same as ENV, it is only here to provide compability with systems
             that use it to indicate running environment.

honeybadger.api_key or HONEYBADGER_API_KEY
-------------
Sample Value: 29facd41
Required?: Yes
Default Value: N/A 
Description: The API key found in the settings tab in the Honeybadger UI.

honeybadger.url
-------------
Sample Value: https://alternative.host
Required?: No
Default Value: https://api.honeybadger.io
Description: URL to the Honeybadger API endpoint. You may want to access it 
             without SSL in order to test with a proxy utility.

honeybadger.application_package
-------------
Sample Value: com.tech.digital.donkey
Required?: No
Default Value: N/A
Description: Java application package name used to indicate to Honeybadger what
             stacktraces are within the calling application's code bade.

http.proxyHost
-------------
Sample Value: localhost
Required?: No
Default Value: N/A
Description: Standard Java system property for specifying the host to proxy all 
             HTTP traffic through.

http.proxyPort
-------------
Sample Value: 8888
Required?: No
Default Value: N/A 
Description: Standard Java system property for specifying the port to proxy all 
             HTTP traffic through.

honeybadger.excluded_exception_classes
-------------
Sample Value: org.apache.catalina.connector.ClientAbortException,com.myorg.AnnoyingException
Required?: No
Default Value: N/A
Description: CSV of Java classes in which errors are never sent to Honeybadger. 
             This is useful for errors that are bubbled up from underlying 
             frameworks or application servers like Tomcat.

honeybadger.excluded_sys_props
-------------
Sample Value: bonecp.password,bonecp.username
Required?: No
Default Value: honeybadger.api_key,honeybadger.excluded_sys_props,honeybadger.url
Description: CSV of Java system properties to exclude from being logged to 
             Honeybadger. This is useful for excluding authentication information.
             
honeybadger.excluded_params
-------------
Sample Value: auth_token,session_data,credit_card_number
Required?: No
Default Value: N/A
Description: CSV of HTTP GET/POST query parameter values that will be excluded 
             from the data sent to Honeybadger. This is useful for excluding
             authentication information, parameters that are too long or 
             sensitive.
             
honeybadger.read_api_key or HONEYBADGER_READ_API_KEY
-------------
Sample Value: qjcp6c7Nv9yR-bsvGZ77
Required?: Only when running integration tests
Default Value: N/A
Description: API key used to access the Read API

honeybadger.display_feedback_form
-------------
Sample Value: false
Required?: No
Default Value: true
Description: Displays the feedback form or JSON output when an error is thrown
             via a servlet call.
             
honeybadger.feedback_form_template_path
-------------
Sample Value: templates/my-company.mustache
Required?: No
Default Value: templates/feedback-form.mustache
Description: Path within the class path to the mustache template that is displayed
             when an error occurs in a servlet request.
```

## Credits

Originally forked by [Elijah Zupancic](https://github.com/dekobon) from
[honeybadger-java](https://github.com/styleseek/honeybadger-java) - thanks to
both of you for doing the hard work of getting this library started.
