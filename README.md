honeybadger-java
================

Java Client to report exceptions to Honeybadger.io

By adding the following line to your code(Possibly in the main function). All errors that you don't catch will be caught and sent to honeybadger.

    Honeybadger honeybadger = new Honeybadger();

In order for this to work, you must have the following environmental variables set.
    HONEYBADGER_API_KEY
    RACK_ENV

HONEYBADGER_API_KEY: Contains your Honeybadger api key. This is found in your projects settings on the Honeybadger web page.

RACK_ENV: Describes the environment where your program is running. I use development and production.