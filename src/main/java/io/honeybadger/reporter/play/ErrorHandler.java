package io.honeybadger.reporter.play;

import play.api.Configuration;
import play.api.Environment;
import play.api.OptionalSourceMapper;
import play.api.http.DefaultHttpErrorHandler;
import play.api.http.HttpErrorHandler;
import play.api.mvc.RequestHeader;
import play.api.mvc.Result;
import play.api.routing.Router;
import play.core.SourceMapper;
import scala.Function0;
import scala.Option;
import scala.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Error handler for the Play Framework.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class ErrorHandler implements HttpErrorHandler {
    public ErrorHandler() {
    }

    @Override
    public Future<Result> onClientError(RequestHeader request,
                                        int statusCode,
                                        String message) {
        return null;
    }

    @Override
    public Future<Result> onServerError(RequestHeader request,
                                        Throwable exception) {
        return null;
    }

    @Override
    public String onClientError$default$3() {
        return null;
    }
}
