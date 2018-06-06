package io.honeybadger.reporter.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.ACCEPT_CHARSET;
import static org.apache.http.HttpHeaders.ACCEPT_ENCODING;
import static org.apache.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_LENGTH;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpHeaders.USER_AGENT;

/**
 * CGI parameters passed to the server when the error occurred.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class CgiData extends LinkedHashMap<String, Object>
        implements Serializable {
    private static final long serialVersionUID = 1006793090880571738L;

    public static final String AUTH_TYPE_KEY = "AUTH_TYPE";
    public static final String CONTENT_LENGTH_KEY = "CONTENT_LENGTH";
    public static final String CONTENT_TYPE_KEY = "CONTENT_TYPE";
    public static final String GATEWAY_INTERFACE_KEY = "GATEWAY_INTERFACE";
    public static final String HTTP_ACCEPT_KEY = "HTTP_ACCEPT";
    public static final String HTTP_ACCEPT_CHARSET_KEY = "HTTP_ACCEPT_CHARSET";
    public static final String HTTP_ACCEPT_ENCODING_KEY = "HTTP_ACCEPT_ENCODING";
    public static final String HTTP_ACCEPT_LANGUAGE_KEY = "HTTP_ACCEPT_LANGUAGE";
    public static final String HTTP_COOKIE_KEY = "HTTP_COOKIE";
    public static final String HTTP_USER_AGENT_KEY = "HTTP_USER_AGENT";
    public static final String PATH_INFO_KEY = "PATH_INFO";
    public static final String PATH_TRANSLATED_KEY = "PATH_TRANSLATED";
    public static final String QUERY_STRING_KEY = "QUERY_STRING";
    public static final String REMOTE_ADDR_KEY = "REMOTE_ADDR";
    public static final String REMOTE_HOST_KEY = "REMOTE_HOST";
    public static final String REMOTE_IDENT_KEY = "REMOTE_IDENT";
    public static final String REMOTE_PORT_KEY = "REMOTE_PORT";
    public static final String REMOTE_USER_KEY = "REMOTE_USER";
    public static final String REQUEST_METHOD_KEY = "REQUEST_METHOD";
    public static final String SCRIPT_NAME_KEY = "SCRIPT_NAME";
    public static final String SERVER_NAME_KEY = "SERVER_NAME";
    public static final String SERVER_PORT_KEY = "SERVER_PORT";
    public static final String SERVER_PROTOCOL_KEY = "SERVER_PROTOCOL";
    public static final String SERVER_SOFTWARE_KEY = "SERVER_SOFTWARE";

    /**
     * Mapping of HTTP headers that have known corresponding CGI values that
     * have a 1:1 conversion.
     */
    public static final Map<String, String> HTTP_HEADER_NAMES_TO_CGI;

    static {
        final HashMap<String, String> cgiToHttp = new HashMap<>(8);
        cgiToHttp.put(AUTHORIZATION.toLowerCase(), AUTH_TYPE_KEY);
        cgiToHttp.put(CONTENT_LENGTH.toLowerCase(), CONTENT_LENGTH_KEY);
        cgiToHttp.put(CONTENT_TYPE.toLowerCase(), CONTENT_TYPE_KEY);
        cgiToHttp.put(ACCEPT_CHARSET.toLowerCase(), HTTP_ACCEPT_CHARSET_KEY);
        cgiToHttp.put(ACCEPT_ENCODING.toLowerCase(), HTTP_ACCEPT_ENCODING_KEY);
        cgiToHttp.put(ACCEPT.toLowerCase(), HTTP_ACCEPT_KEY);
        cgiToHttp.put(ACCEPT_LANGUAGE.toLowerCase(), HTTP_ACCEPT_LANGUAGE_KEY);
        cgiToHttp.put(USER_AGENT.toLowerCase(), HTTP_USER_AGENT_KEY);

        HTTP_HEADER_NAMES_TO_CGI = Collections.unmodifiableMap(cgiToHttp);
    }


    public CgiData() {
    }

    public Map<String, String> addFromHttpHeaders(final Map<String, String> headers) {
        return addFromHttpHeaders(headers, Objects::toString);
    }

    public <V> Map<String, V> addFromHttpHeaders(final Map<String, V> headers,
                                                 final Function<V, String> toStringFunction) {
        final Set<Map.Entry<String, V>> entries = headers.entrySet();
        final Map<String, V> notMatched = new LinkedHashMap<>(entries.size());

        for (Map.Entry<String, V> entry : entries) {
            final String key = entry.getKey().toLowerCase();
            final String cgiKey = HTTP_HEADER_NAMES_TO_CGI.get(key);

            if (cgiKey != null) {
                put(cgiKey, toStringFunction.apply(entry.getValue()));
            } else {
                notMatched.put(entry.getKey(), entry.getValue());
            }
        }

        return Collections.unmodifiableMap(notMatched);
    }

    public CgiData setAuthType(final Object accept) {
        put(HTTP_ACCEPT_KEY, accept);
        return this;
    }

    public CgiData setHttpAccept(final Object acceptCharset) {
        put(HTTP_ACCEPT_CHARSET_KEY, acceptCharset);
        return this;
    }

    public CgiData setContentLength(final Object contentLength) {
        put(CONTENT_LENGTH_KEY, contentLength);
        return this;
    }

    public CgiData setContentLength(final long contentLength) {
        put(CONTENT_LENGTH_KEY, Long.toString(contentLength));
        return this;
    }

    public CgiData setContentType(final Object contentType) {
        put(CONTENT_TYPE_KEY, contentType);
        return this;
    }

    public CgiData setGatewayInterface(final Object gatewayInterface) {
        put(GATEWAY_INTERFACE_KEY, gatewayInterface);
        return this;
    }

    public CgiData setHttpAcceptCharset(final Object acceptCharset) {
        put(HTTP_ACCEPT_CHARSET_KEY, acceptCharset);
        return this;
    }

    public CgiData setHttpAcceptLanguage(final Object acceptLanguage) {
        put(HTTP_ACCEPT_LANGUAGE_KEY, acceptLanguage);
        return this;
    }

    public CgiData setHttpAcceptEncoding(final Object acceptEncoding) {
        put(HTTP_ACCEPT_ENCODING_KEY, acceptEncoding);
        return this;
    }

    public CgiData setHttpCookie(final Object cookie) {
        put(HTTP_COOKIE_KEY, cookie);
        return this;
    }

    public CgiData setHttpUserAgent(final Object userAgent) {
        put(HTTP_USER_AGENT_KEY, userAgent);
        return this;
    }

    public CgiData setPathInfo(final Object pathInfo) {
        put(PATH_INFO_KEY, pathInfo);
        return this;
    }

    public CgiData setPathTranslated(final Object pathTranslated) {
        put(PATH_TRANSLATED_KEY, pathTranslated);
        return this;
    }

    public CgiData setQueryString(final Object queryString) {
        put(QUERY_STRING_KEY, queryString);
        return this;
    }

    public CgiData setRemoteAddr(final Object remoteAddr) {
        put(REMOTE_ADDR_KEY, remoteAddr);
        return this;
    }

    public CgiData setRemoteHost(final Object remoteHost) {
        put(REMOTE_HOST_KEY, remoteHost);
        return this;
    }

    public CgiData setRemoteIdent(final Object remoteIdent) {
        put(REMOTE_IDENT_KEY, remoteIdent);
        return this;
    }

    public CgiData setRemotePort(final Object remotePort) {
        put(REMOTE_PORT_KEY, remotePort);
        return this;
    }

    public CgiData setRemoteUser(final Object remoteUser) {
        put(REMOTE_USER_KEY, remoteUser);
        return this;
    }

    public CgiData setRequestMethod(final Object requestMethod) {
        put(REQUEST_METHOD_KEY, requestMethod);
        return this;
    }

    public CgiData setScriptName(final Object scriptName) {
        put(SCRIPT_NAME_KEY, scriptName);
        return this;
    }

    public CgiData setServerName(final Object serverName) {
        put(SERVER_NAME_KEY, serverName);
        return this;
    }

    public CgiData setServerPort(final Object serverPort) {
        put(SERVER_PORT_KEY, serverPort);
        return this;
    }

    public CgiData setServerProtocol(final Object serverProtocol) {
        put(SERVER_PROTOCOL_KEY, serverProtocol);
        return this;
    }

    public CgiData setServerSoftware(final Object serverSoftware) {
        put(SERVER_SOFTWARE_KEY, serverSoftware);
        return this;
    }

    public Integer getAsInteger(final String key) {
        Object value = get(key);

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return null;
    }
}
