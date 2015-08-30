package io.honeybadger.reporter.dto;

import org.apache.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Map;

import static io.honeybadger.reporter.dto.RequestParsingUtils.parseParamsFromMap;

/**
 * Factory class that creates a {@link Request} based on a 
 * {@link javax.servlet.http.HttpServletRequest}.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class HttpServletRequestFactory {
    public static Request create(HttpServletRequest httpRequest) {
        Context context = createContext(httpRequest);
        String url = getFullURL(httpRequest);
        Params params = createParams(httpRequest);
        Session session = createSession(httpRequest);
        CgiData cgi_data = createCgiData(httpRequest);

        return new Request(context, url, params, session, cgi_data);
    }

    protected static Context createContext(HttpServletRequest httpRequest) {
        Context context = new Context();

        Principal principal = httpRequest.getUserPrincipal();

        if (principal != null) {
            context.put("user_name", principal.getName());
        }
        
        return context;
    }
    
    protected static Params createParams(HttpServletRequest httpRequest) {
        return parseParamsFromMap(httpRequest.getParameterMap());
    }
    
    protected static Session createSession(HttpServletRequest httpRequest) {
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
    
    protected static CgiData createCgiData(HttpServletRequest httpRequest) {
        final CgiData cgiData = new CgiData();

        cgiData.put("REQUEST_METHOD", httpRequest.getMethod());
        cgiData.put("HTTP_ACCEPT", httpRequest.getHeader(HttpHeaders.ACCEPT));
        cgiData.put("HTTP_USER_AGENT", httpRequest.getHeader(HttpHeaders.USER_AGENT));
        cgiData.put("HTTP_ACCEPT_ENCODING", httpRequest.getHeader(HttpHeaders.ACCEPT_ENCODING));
        cgiData.put("HTTP_ACCEPT_LANGUAGE", httpRequest.getHeader(HttpHeaders.ACCEPT_LANGUAGE));
        cgiData.put("HTTP_ACCEPT_CHARSET", httpRequest.getHeader(HttpHeaders.ACCEPT_CHARSET));
        cgiData.put("HTTP_COOKIE", parseCookies(httpRequest));
        cgiData.put("SERVER_NAME", httpRequest.getServerName());
        cgiData.put("SERVER_PORT", httpRequest.getServerPort());
        cgiData.put("CONTENT_TYPE", httpRequest.getContentType());
        cgiData.put("CONTENT_LENGTH", httpRequest.getContentLength());
        cgiData.put("REMOTE_ADDR", httpRequest.getRemoteAddr());
        cgiData.put("REMOTE_PORT", httpRequest.getRemotePort());
        cgiData.put("QUERY_STRING", httpRequest.getQueryString());
        cgiData.put("PATH_INFO", httpRequest.getPathInfo());
        
        return cgiData;
    }

    static String parseCookies(HttpServletRequest request) {
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
    protected static String getFullURL(HttpServletRequest request) {
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
