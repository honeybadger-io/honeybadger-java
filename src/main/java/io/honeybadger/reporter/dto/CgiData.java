package io.honeybadger.reporter.dto;

import org.apache.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * CGI parameters passed to the server when the error occurred.
 */
public class CgiData extends LinkedHashMap<String, Object> 
        implements Serializable {
    private static final long serialVersionUID = 1006793090880571738L;

    public CgiData(HttpServletRequest request) {
        addCgiParams(request);
    }
    
    void addCgiParams(HttpServletRequest request) {
        put("REQUEST_METHOD", request.getMethod());
        put("HTTP_ACCEPT", request.getHeader(HttpHeaders.ACCEPT));
        put("HTTP_USER_AGENT", request.getHeader(HttpHeaders.USER_AGENT));
        put("HTTP_ACCEPT_ENCODING", request.getHeader(HttpHeaders.ACCEPT_ENCODING));
        put("HTTP_ACCEPT_LANGUAGE", request.getHeader(HttpHeaders.ACCEPT_LANGUAGE));
        put("HTTP_ACCEPT_CHARSET", request.getHeader(HttpHeaders.ACCEPT_CHARSET));
        put("SERVER_NAME", request.getServerName());
        put("SERVER_PORT", request.getServerPort());
        put("CONTENT_TYPE", request.getContentType());
        put("CONTENT_LENGTH", request.getContentLength());
        put("REMOTE_ADDR", request.getRemoteAddr());
        put("REMOTE_PORT", request.getRemotePort());
        put("QUERY_STRING", request.getQueryString());
        put("PATH_INFO", request.getPathInfo());
    }
}
