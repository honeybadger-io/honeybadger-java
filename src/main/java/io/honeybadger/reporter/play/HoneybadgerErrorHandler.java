package io.honeybadger.reporter.play;

import io.honeybadger.reporter.HoneybadgerReporter;
import io.honeybadger.reporter.NoticeReportResult;
import io.honeybadger.reporter.NoticeReporter;
import io.honeybadger.reporter.config.ConfigContext;
import io.honeybadger.reporter.config.PlayConfigContext;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.api.OptionalSourceMapper;
import play.api.UsefulException;
import play.api.http.HttpErrorHandlerExceptions;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Error handler for the Play Framework.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class HoneybadgerErrorHandler extends DefaultHttpErrorHandler {
    protected final NoticeReporter reporter;
    protected final Environment environment;
    protected final OptionalSourceMapper sourceMapper;

    @Inject
    public HoneybadgerErrorHandler(Configuration configuration,
                                   Environment environment,
                                   OptionalSourceMapper sourceMapper,
                                   Provider<Router> routes) {
        super(configuration, environment, sourceMapper, routes);
        this.environment = environment;
        this.sourceMapper = sourceMapper;
        ConfigContext context = new PlayConfigContext(configuration, environment);
        reporter = new HoneybadgerReporter(context);
    }

    @Override
    public CompletionStage<Result> onServerError(Http.RequestHeader request, Throwable exception) {

        NoticeReportResult errorResult = reporter.reportError(exception, request);

        try {
            UsefulException usefulException = throwableToUsefulException(exception);
            final String honeybadgerErrorId;

            if (errorResult != null && errorResult.getId() != null) {
                honeybadgerErrorId = errorResult.getId().toString();
            } else {
                honeybadgerErrorId = String.format("play-error-%s", usefulException.id);
            }

            // Overwrite play exception id with Honeybadger ID
            usefulException.id = honeybadgerErrorId;

            logServerError(request, usefulException);

            switch (environment.mode()) {
                case PROD:
                    return onProdServerError(request, usefulException);
                default:
                    return onDevServerError(request, usefulException);
            }
        } catch (Exception e) {
            Logger.error("Error while handling error", e);
            return CompletableFuture.completedFuture(Results.internalServerError());
        }
    }

    /**
     * Convert the given exception to an exception that Play can report more information about.
     *
     * This will generate an id for the exception, and in dev mode, will load the source code for the code that threw the
     * exception, making it possible to report on the location that the exception was thrown from.
     *
     * @param throwable exception being converted to UsefulException
     * @return input throwable represented as UsefulException
     */
    protected UsefulException throwableToUsefulException(final Throwable throwable) {
        return HttpErrorHandlerExceptions.throwableToUsefulException(
                sourceMapper.sourceMapper(),
                environment.isProd(),
                throwable);
    }
}
