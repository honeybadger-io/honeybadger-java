package io.honeybadger.reporter.dto;

import io.honeybadger.reporter.config.ConfigContext;
import org.apache.http.HttpHeaders;
import play.mvc.Http;

import java.util.Iterator;

/**
 * Factory class that creates a {@link Request} based on a
 * {@link play.mvc.Http.Request}.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class PlayHttpRequestFactory {
    public static Request create(ConfigContext config,
                                 Http.Request httpRequest) {
        Context context = createContext(httpRequest);
        String url = getFullURL(httpRequest);
        Params params = createParams(config, httpRequest);
        Session session = createSession(httpRequest);
        CgiData cgi_data = createCgiData(httpRequest);

        return new Request(context, url, params, session, cgi_data);
    }

    protected static Context createContext(Http.Request httpRequest) {
        Context context = new Context();

        String username =  httpRequest.username();

        if (username != null) {
            context.put("user_name", username);
        }

        return context;
    }

    protected static String getFullURL(Http.Request httpRequest) {
        return httpRequest.uri();
    }

    protected static Params createParams(ConfigContext config,
                                         Http.Request httpRequest) {
        Http.RequestBody body = httpRequest.body();

        if (body == null) return new Params(config.getExcludedParams());

        return Params.parseParamsFromMap(config.getExcludedParams(),
                                  body.asFormUrlEncoded());
    }

    protected static Session createSession(Http.Request httpRequest) {
        final Session session = new Session();

        // We don't support Play sessions or flash scopes yet
        // Please write me if you need me.

        return session;
    }

    protected static CgiData createCgiData(Http.Request httpRequest) {
        final CgiData cgiData = new CgiData();

        cgiData.put("REQUEST_METHOD", httpRequest.method());
        cgiData.put("HTTP_ACCEPT", httpRequest.getHeader(HttpHeaders.ACCEPT));
        cgiData.put("HTTP_USER_AGENT", httpRequest.getHeader(HttpHeaders.USER_AGENT));
        cgiData.put("HTTP_ACCEPT_ENCODING", httpRequest.getHeader(HttpHeaders.ACCEPT_ENCODING));
        cgiData.put("HTTP_ACCEPT_LANGUAGE", httpRequest.getHeader(HttpHeaders.ACCEPT_LANGUAGE));
        cgiData.put("HTTP_ACCEPT_CHARSET", httpRequest.getHeader(HttpHeaders.ACCEPT_CHARSET));
        cgiData.put("HTTP_COOKIE", parseCookies(httpRequest));
        cgiData.put("CONTENT_TYPE", httpRequest.getHeader(HttpHeaders.CONTENT_TYPE));
        cgiData.put("CONTENT_LENGTH", httpRequest.getHeader(HttpHeaders.CONTENT_LENGTH));
        cgiData.put("REMOTE_ADDR", httpRequest.remoteAddress());
        cgiData.put("QUERY_STRING", httpRequest.queryString());
        cgiData.put("PATH_INFO", httpRequest.path());

        if (httpRequest.host() != null && !httpRequest.host().isEmpty()) {
            String[] hostParts = httpRequest.host().split(":");

            if (hostParts.length > 0)  cgiData.put("SERVER_NAME", hostParts[0]);
            if (hostParts.length > 1)  cgiData.put("SERVER_PORT", hostParts[1]);
        }

        return cgiData;
    }

    static String parseCookies(Http.Request httpRequest) {
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
