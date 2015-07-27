package io.honeybadger.reporter.servlet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FakeHttpServletRequest implements HttpServletRequest {
    private Map<String, ArrayList<String>> headers =
            new ConcurrentHashMap<>();

    private String requestUri = "https://www.youtube.com/watch?v=4r7wHMg5Yjg";
    private String method = "GET";
    private String serverName = "www.youtube.com";
    private int serverPort = 80;
    private String contentType = "text/html; charset=UTF-8";
    private HttpSession session = new FakeHttpSession("session-id",
            ImmutableMap.of("session_key_1", (Object) "session_val_1"));

    private Collection<? extends Part> parts = ImmutableList.of(
        new FakePart("testpart", "value")
    );


    public FakeHttpServletRequest() {
    }

    public FakeHttpServletRequest(Map<String, ? extends List<String>> headers) {
        Iterator<Map.Entry<String, List<String>>> itr =
                ((Map<String, List<String>>)headers).entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry<String, List<String>> entry = itr.next();
            this.headers.put(entry.getKey().toLowerCase(),
                             new ArrayList<String>(entry.getValue()));
        }
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        List<Cookie> cookies = new ArrayList<>();
        List<String> cookieValues = headers.get("set-cookie");
        
        if (cookieValues == null) return new Cookie[] {};
        
        for (String c : cookieValues) {;
            for (HttpCookie httpCookie : HttpCookie.parse(c)) {
                cookies.add(copyFromHttpCookie(httpCookie));
            }
        }

        Cookie[] cookieArray = new Cookie[cookies.size()];
        cookies.toArray(cookieArray);
        return cookieArray;
    }
    
    Cookie copyFromHttpCookie(HttpCookie cookie) {
        return new Cookie(cookie.getName(), cookie.getValue());
    }

    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        ArrayList<String> values = headers.get(name.toLowerCase());
        if (values == null || values.isEmpty()) return null;

        return values.get(0);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        ArrayList<String> values = headers.get(name.toLowerCase());
        if (values == null || values.isEmpty()) return null;

        return Collections.enumeration(values);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        if (headers == null || headers.isEmpty()) return null;

        return Collections.enumeration(headers.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        ArrayList<String> values = headers.get(name.toLowerCase());
        if (values == null || values.isEmpty()) return 0;

        try {
            return Integer.parseInt(values.get(0));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return requestUri;
    }

    @Override
    public StringBuffer getRequestURL() {
        String url = getRequestURI();
        return new StringBuffer(url);
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean create) {
        return session;
    }

    @Override
    public HttpSession getSession() {
        return session;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return (Collection<Part>)parts;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }

    @Override
    public String[] getParameterValues(String name) {
        return new String[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object o) {

    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }
}
