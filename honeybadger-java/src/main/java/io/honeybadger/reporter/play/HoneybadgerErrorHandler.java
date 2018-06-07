package io.honeybadger.reporter.play;

import com.typesafe.config.Config;
import io.honeybadger.reporter.HoneybadgerReporter;
import io.honeybadger.reporter.NoticeReportResult;
import io.honeybadger.reporter.NoticeReporter;
import io.honeybadger.reporter.config.ConfigContext;
import io.honeybadger.reporter.config.PlayConfigContext;
import play.Environment;
import play.Logger;
import play.api.OptionalSourceMapper;
import play.api.UsefulException;
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
    private final NoticeReporter reporter;
    private final Environment environment;
    private final OptionalSourceMapper sourceMapper;

    @Inject
    public HoneybadgerErrorHandler(final Config config,
                                   final Environment environment,
                                   final OptionalSourceMapper sourceMapper,
                                   final Provider<Router> routes) {
        super(config, environment, sourceMapper, routes);
        this.environment = environment;
        this.sourceMapper = sourceMapper;

        final ConfigContext context = new PlayConfigContext(config, environment);
        this.reporter = new HoneybadgerReporter(context);
    }

    @Override
    public CompletionStage<Result> onServerError(final Http.RequestHeader request,
                                                 final Throwable exception) {

        NoticeReportResult errorResult = getReporter().reportError(exception, request);

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

            switch (getEnvironment().mode()) {
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

    protected NoticeReporter getReporter() {
        return reporter;
    }

    protected Environment getEnvironment() {
        return environment;
    }

    protected OptionalSourceMapper getSourceMapper() {
        return sourceMapper;
    }
}
