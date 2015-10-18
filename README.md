# Honeybadger for Java
 
[![Build Status](https://travis-ci.org/honeybadger-io/honeybadger-java.svg)](https://travis-ci.org/honeybadger-io/honeybadger-java)

This is the notifier JVM library for integrating applications with the :zap: [Honeybadger.io error notification
service](https://www.honeybadger.io/).
When an uncaught exception occurs, Honeybadger will POST the relevant data to the Honeybadger server specified in your environment.

## Supported JVM versions

| JVM               | Supported Version |
| ----------------- | ----------------- |
| Oracle (Java SE)  | 1.7, 1.8          |
| OpenJDK JDK/JRE   | 1.7, 1.8          |

## Supported web frameworks

| Framework      | Version |
| -------------- | ------- |
| Servlet API    | 3.1.0   |
| Play Framework | 2.4.2   |      

The Play Framework is supported natively (install/configure the library and your done). For the Servlet API, you 
will need to configure a [servlet filter](https://github.com/honeybadger-io/honeybadger-java/blob/master/src/main/java/io/honeybadger/reporter/servlet/HoneybadgerFilter.java) and enable it in your application. As for manual invocation of the API, you will need to configure your application to directly call the [reporter class](https://github.com/honeybadger-io/honeybadger-java/blob/master/src/main/java/io/honeybadger/reporter/HoneybadgerReporter.java). You can find more information about this in the stand-alone usage section.

## Getting Started

Honeybadger works out of the box with many popular Java frameworks. Installation is just a matter of including the 
jar library and setting your API key. In this section, we'll cover the basics. More advanced installations are 
covered later. 

### 1. Install the jar

The first step is to add the honeybadger jar to your dependency manager (Maven, SBT, Gradle, Ivy, etc). 
In the case of Maven, you would add it as so:

```xml
<dependency>
    <groupId>io.honeybadger</groupId>
    <artifactId>honeybadger-java</artifactId>
    <!-- Set the specific version below in order to ensure API compatibility in future versions -->
    <version>LATEST</version>
</dependency>
```

In the case of SBT:

```
libraryDependencies += "io.honeybadger" % "honeybadger-java" % "<<VERSION NUMBER>>"
```

For other dependency managers an example is provided on the [Maven Central site](http://search.maven.org/#artifactdetails%7Cio.honeybadger%7Choneybadger-java%7C1.0.10%7Cjar).

If you are not using a dependency manager, [download the jar directly](http://search.maven.org/remotecontent?filepath=io/honeybadger/honeybadger-java/1.0.10/honeybadger-java-1.0.10.jar) and add it to your classpath.

### 2. Install a slf4j compatible logging library or binding in your project

All dependencies needed for running are included in the distributed JAR with one
exception - slf4j-api. We expect that you are using some logging library and that
you have imported the sl4j-api in order to provide a common interface for the 
logger to imported libraries.

Almost every logging library provides a means for it to be compatible with the slf4j API. 
These are two good candidates if you aren't sure about which one to choose:

 * [Logback](http://logback.qos.ch/)
 * [log4j2](http://logging.apache.org/log4j/2.x/log4j-slf4j-impl/index.html)

### 3. Set your API key and configuration parameters

Next, you'll set the API key and some configuration parameters for this project.

#### Stand-alone Usage

If you want to send all unhandled errors to Honeybadger and have them logged to slf4j via 
the error log level, you will need to set the correct system properties (or provide a [ConfigContext](https://github.com/honeybadger-io/honeybadger-java/blob/master/src/main/java/io/honeybadger/reporter/config/ConfigContext.java)) and add a single line to the thread in which you want to register the error handler.

A typical stand-alone implementation may look like:

```java
import io.honeybadger.reporter.HoneybadgerUncaughtExceptionHandler;

public class MyApp {
 public static void main(String argv[]) {
     HoneybadgerUncaughtExceptionHandler.registerAsUncaughtExceptionHandler();
     // The rest of the application goes here
 }
}
```

You would invoke it with the ```-Dhoneybadger.api_key=<my_api_key>``` system parameter and any other configuration values via system parameters it would load with the correct state. It would then register itself as the default error handler.

#### Servlet Usage

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

**Note: If you have other code executing in your servlet-based application that doesn't go through the servlet interface, you will want to register an exception handler for it in order to report errors to Honeybadger. See the *Stand-alone Usage* section.**

#### Play Framework Usage

This library has been tested against Play 2.4.2. You can add Honeybadger as an error
handler by adding the following lines to your conf/application.conf file:

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

## Advanced Configuration

There are a few ways to configure the Honeybadger library. Each one of the ways is implemented as a [ConfigContext](https://github.com/honeybadger-io/honeybadger-java/blob/master/src/main/java/io/honeybadger/reporter/config/ConfigContext.java) that can be passed in the constructor of the [HoneybadgerReporter](https://github.com/honeybadger-io/honeybadger-java/blob/master/src/main/java/io/honeybadger/reporter/HoneybadgerReporter.java) class. The implementations available are:

 * [StandardConfigContext](https://github.com/honeybadger-io/honeybadger-java/blob/master/src/main/java/io/honeybadger/reporter/config/StandardConfigContext.java) - This reads configuration from the system parameters, environment variables and defaults and is **the default configuration provider**.
 * [PlayConfigContext](https://github.com/honeybadger-io/honeybadger-java/blob/master/src/main/java/io/honeybadger/reporter/config/PlayConfigContext.java) - This reads configuration from the Play Framework's internal configuration mechanism.
 * [ServletFilterConfigContext](https://github.com/honeybadger-io/honeybadger-java/blob/master/src/main/java/io/honeybadger/reporter/config/ServletFilterConfigContext.java) - This reads configuration from a servlet filter configuration.
 * [SystemSettingsConfigContext](https://github.com/honeybadger-io/honeybadger-java/blob/master/src/main/java/io/honeybadger/reporter/config/SystemSettingsConfigContext.java) - This reads configuration purely from system settings.
 * [MapConfigContext](https://github.com/honeybadger-io/honeybadger-java/blob/master/src/main/java/io/honeybadger/reporter/config/MapConfigContext.java) - This reads configuration from a Map that is supplied to its constructor.

### Configuring with Environment Variables (12-factor style)

All configuration options can also be read from environment variables or [Java system properties](https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html).

## Configuration Options

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

## Credits

Originally forked by [Elijah Zupancic](https://github.com/dekobon) from
[honeybadger-java](https://github.com/styleseek/honeybadger-java) - thanks to
both of you for doing the hard work of getting this library started.
