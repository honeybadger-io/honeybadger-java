package com.github.dekobon.honeybadger.servlet;

import com.github.dekobon.honeybadger.HoneybadgerException;
import com.github.dekobon.honeybadger.RequestInfoGenerator;
import com.google.gson.JsonObject;
import org.apache.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Parses the properties of a {@link javax.servlet.http.HttpServletRequest}
 * object and turns it into the Honeybadger JSON format.
 *
 * @author <a href="https://github.com/dekobon">dekobon</a>
 * @since 1.0.0
 */
public class HttpServletRequestInfoGenerator
        implements RequestInfoGenerator<HttpServletRequest> {
    @Override
    public JsonObject generateRequest(HttpServletRequest request) {
        JsonObject jsonRequest = new JsonObject();

        jsonRequest.addProperty("url", getFullURL(request));
        jsonRequest.add("cgi_data", cgiData(request));
        jsonRequest.add("params", params(request));

        return jsonRequest;
    }

    @Override
    public JsonObject routeRequest(Object requestSource) {
        if (!(requestSource instanceof HttpServletRequest)) {
            throw new HoneybadgerException("Request object is not instance " +
                    "of HttpServletRequest");
        }
        return generateRequest((HttpServletRequest) requestSource);
    }

    protected JsonObject cgiData(HttpServletRequest request) {
        JsonObject cgiData = new JsonObject();

        cgiData.addProperty("REQUEST_METHOD", request.getMethod());
        cgiData.addProperty("HTTP_ACCEPT", request.getHeader(HttpHeaders.ACCEPT));
        cgiData.addProperty("HTTP_USER_AGENT", request.getHeader(HttpHeaders.USER_AGENT));
        cgiData.addProperty("HTTP_ACCEPT_ENCODING", request.getHeader(HttpHeaders.ACCEPT_ENCODING));
        cgiData.addProperty("HTTP_ACCEPT_LANGUAGE", request.getHeader(HttpHeaders.ACCEPT_LANGUAGE));
        cgiData.addProperty("HTTP_ACCEPT_CHARSET", request.getHeader(HttpHeaders.ACCEPT_CHARSET));
        cgiData.addProperty("SERVER_NAME", request.getServerName());
        cgiData.addProperty("SERVER_PORT", request.getServerPort());
        cgiData.addProperty("CONTENT_TYPE", request.getContentType());
        cgiData.addProperty("CONTENT_LENGTH", request.getContentLength());
        cgiData.addProperty("REMOTE_ADDR", request.getRemoteAddr());
        cgiData.addProperty("REMOTE_PORT", request.getRemotePort());
        cgiData.addProperty("QUERY_STRING", request.getQueryString());
        cgiData.addProperty("PATH_INFO", request.getPathInfo());

        return cgiData;
    }

    protected JsonObject params(HttpServletRequest request) {
        JsonObject jsonParams = new JsonObject();

        jsonParams.add("request_headers", httpHeaders(request));

        return jsonParams;
    }

    protected JsonObject httpHeaders(HttpServletRequest request) {
        JsonObject jsonHeaders = new JsonObject();

        Enumeration<String> headers = request.getHeaderNames();

        if (headers != null) {
            while (headers.hasMoreElements()) {
                String name = headers.nextElement();
                jsonHeaders.addProperty(name, request.getHeader(name));
            }
        }

        return jsonHeaders;
    }

    /**
     * Gets the fully formed URL for a servlet request.
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
