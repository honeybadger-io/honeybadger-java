package io.honeybadger.reporter.dto;

import io.honeybadger.reporter.config.ConfigContext;
import org.apache.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Enumeration;

/**
 * Factory class that creates a {@link Request} based on a
 * {@link javax.servlet.http.HttpServletRequest}.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public final class HttpServletRequestFactory {
    private HttpServletRequestFactory() { }

    public static Request create(final ConfigContext config,
                                 final HttpServletRequest httpRequest) {
        Context context = createContext(httpRequest);
        String url = getFullURL(httpRequest);
        Params params = createParams(config, httpRequest);
        Session session = createSession(httpRequest);
        CgiData cgi_data = createCgiData(httpRequest);

        return new Request(context, url, params, session, cgi_data);
    }

    protected static Context createContext(final HttpServletRequest httpRequest) {
        Context context = new Context();

        Principal principal = httpRequest.getUserPrincipal();

        if (principal != null) {
            context.setUsername(principal.getName());
        }

        return context;
    }

    protected static Params createParams(final ConfigContext config,
                                         final HttpServletRequest httpRequest) {
        return Params.parseParamsFromMap(config.getExcludedParams(),
                httpRequest.getParameterMap());
    }

    protected static Session createSession(final HttpServletRequest httpRequest) {
        final Session session = new Session();
        final HttpSession httpSession = httpRequest.getSession();

        if (httpSession == null) return session;

        try {
            session.put("session_id", httpSession.getId());
            session.put("creation_time", httpSession.getCreationTime());

            final Enumeration<String> attributes = httpSession.getAttributeNames();

            while (attributes.hasMoreElements()) {
                final String key = attributes.nextElement();
                final Object value = httpSession.getAttribute(key);
                if (value == null) continue;
                final String valueAsString = String.valueOf(value);
                final String subString = valueAsString.length() > Session.MAX_SESSION_OBJ_STRING_SIZE ?
                        valueAsString.substring(0, Session.MAX_SESSION_OBJ_STRING_SIZE) :
                        valueAsString;

                session.put(key, subString);
            }
        } catch (RuntimeException e) {
            session.put("Error getting session", e.getMessage());
        }

        return session;
    }

    protected static CgiData createCgiData(final HttpServletRequest httpRequest) {
        final CgiData cgiData = new CgiData()
                .setRequestMethod(httpRequest.getMethod())
                .setHttpAccept(httpRequest.getHeader(HttpHeaders.ACCEPT))
                .setHttpUserAgent(httpRequest.getHeader(HttpHeaders.USER_AGENT))
                .setHttpAcceptEncoding(httpRequest.getHeader(HttpHeaders.ACCEPT_ENCODING))
                .setHttpAcceptLanguage(httpRequest.getHeader(HttpHeaders.ACCEPT_LANGUAGE))
                .setHttpAcceptCharset(httpRequest.getHeader(HttpHeaders.ACCEPT_CHARSET))
                .setHttpCookie(parseCookies(httpRequest))
                .setServerName(httpRequest.getServerName())
                .setServerPort(httpRequest.getServerPort())
                .setContentType(httpRequest.getContentType())
                .setContentLength(httpRequest.getContentLength())
                .setRemoteAddr(httpRequest.getRemoteAddr())
                .setRemotePort(httpRequest.getRemotePort())
                .setQueryString(httpRequest.getQueryString())
                .setPathInfo(httpRequest.getPathInfo())
                .setPathTranslated(httpRequest.getPathTranslated())
                .setServerProtocol(httpRequest.getProtocol());

        return cgiData;
    }

    static String parseCookies(final HttpServletRequest request) {
        Enumeration<String> cookies = request.getHeaders("Set-Cookie");

        if (cookies == null || !cookies.hasMoreElements()) return null;

        StringBuilder builder = new StringBuilder();

        while (cookies.hasMoreElements()) {
            String c = cookies.nextElement();
            if (c == null) continue;

            builder.append(c.trim());

            if (cookies.hasMoreElements()) {
                builder.append("; ");
            }
        }

        return builder.toString();
    }

    /** Gets the fully formed URL for a servlet request.
     * @see <a href="http://stackoverflow.com/a/2222268/33611">Stack Overflow Answer</a>
     * @param request Servlet request to parse for URL information
     * @return fully formed URL as string
     */
    protected static String getFullURL(final HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (requestURL == null) {
            return null;
        } else if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }
}
