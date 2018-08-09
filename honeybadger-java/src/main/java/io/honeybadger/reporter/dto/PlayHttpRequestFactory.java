package io.honeybadger.reporter.dto;

import io.honeybadger.reporter.config.ConfigContext;
import org.apache.http.HttpHeaders;
import play.mvc.Http;
import play.mvc.Security;

import java.util.Iterator;
import java.util.Optional;

/**
 * Factory class that creates a {@link Request} based on a
 * {@link play.mvc.Http.Request}.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public final class PlayHttpRequestFactory {
    private PlayHttpRequestFactory() { }

    public static Request create(final ConfigContext config,
                                 final Http.Request httpRequest) {
        Context context = createContext(httpRequest);
        String url = getFullURL(httpRequest);
        Params params = createParams(config, httpRequest);
        Session session = createSession(httpRequest);
        CgiData cgiData = createCgiData(httpRequest);

        return new Request(context, url, params, session, cgiData);
    }

    protected static Context createContext(final Http.Request httpRequest) {
        final Context context = new Context();
        final Optional<String> username = httpRequest.attrs().getOptional(Security.USERNAME);

        username.ifPresent(context::setUsername);

        return context;
    }

    protected static String getFullURL(final Http.Request httpRequest) {
        return httpRequest.uri();
    }

    protected static Params createParams(final ConfigContext config,
                                         final Http.Request httpRequest) {
        Http.RequestBody body = httpRequest.body();

        if (body == null) return new Params(config.getExcludedParams());

        return Params.parseParamsFromMap(config.getExcludedParams(),
                                  body.asFormUrlEncoded());
    }

    protected static Session createSession(final Http.Request httpRequest) {
        final Session session = new Session();

        // We don't support Play sessions or flash scopes yet
        // Please write me if you need me.

        return session;
    }

    @SuppressWarnings("StringSplitter")
    protected static CgiData createCgiData(final Http.Request httpRequest) {
        final CgiData cgiData = new CgiData()
                .setRequestMethod(httpRequest.method())
                .setContentLength(getHeaderValue(httpRequest, HttpHeaders.CONTENT_LENGTH))
                .setHttpAccept(getHeaderValue(httpRequest, HttpHeaders.ACCEPT))
                .setHttpUserAgent(getHeaderValue(httpRequest, HttpHeaders.USER_AGENT))
                .setHttpAcceptCharset(getHeaderValue(httpRequest, HttpHeaders.ACCEPT_CHARSET))
                .setHttpAcceptEncoding(getHeaderValue(httpRequest, HttpHeaders.ACCEPT_ENCODING))
                .setHttpAcceptLanguage(getHeaderValue(httpRequest, HttpHeaders.ACCEPT_LANGUAGE))
                .setHttpCookie(parseCookies(httpRequest))
                .setContentLength(getHeaderValue(httpRequest, HttpHeaders.CONTENT_LENGTH))
                .setContentType(getHeaderValue(httpRequest, HttpHeaders.CONTENT_TYPE))
                .setRemoteAddr(httpRequest.remoteAddress())
                .setQueryString(httpRequest.queryString())
                .setPathInfo(httpRequest.path());

        if (httpRequest.host() != null && !httpRequest.host().isEmpty()) {
            final String[] hostParts = httpRequest.host().split(":");

            if (hostParts.length > 0) {
                cgiData.setServerName(hostParts[0]);
            }
            if (hostParts.length > 1) {
                cgiData.setServerPort(hostParts[1]);
            }
        }

        return cgiData;
    }

    static Object getHeaderValue(final Http.Request httpRequest, final String key) {
        final Http.Headers headers = httpRequest.getHeaders();
        return headers.get(key).orElse(null);
    }

    static String parseCookies(final Http.Request httpRequest) {
        Http.Cookies cookies = httpRequest.cookies();

        Iterator<Http.Cookie> itr = cookies.iterator();

        if (cookies == null || !itr.hasNext()) return null;

        StringBuilder builder = new StringBuilder();

        while (itr.hasNext()) {
            Http.Cookie next = itr.next();
            String c = String.format("%s=%s", next.name(), next.value());

            builder.append(c.trim());

            if (itr.hasNext()) {
                builder.append("; ");
            }
        }

        return builder.toString();
    }
}
