package io.honeybadger.reporter.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fake implementation of {@link javax.servlet.http.HttpSession} for testing.
 */
@SuppressWarnings("deprecation")
public class FakeHttpSession implements HttpSession {
    private Map<String, Object> attributes;
    private long creationTime;
    private String id;
    private long lastAccessedTime;

    public FakeHttpSession(String id, Map<String, Object> attributes) {
        this.id = id;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = creationTime + 2L;
        this.attributes = new ConcurrentHashMap<>(attributes);
    }

    public FakeHttpSession(String id) {
        this(id, new HashMap<String, Object>());
    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getMaxInactiveInterval() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Object getValue(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String[] getValueNames() {
        final String[] names = new String[attributes.size()];
        attributes.keySet().toArray(names);
        return names;
    }

    @Override
    public void setAttribute(String name, Object value) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void putValue(String name, Object value) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void removeAttribute(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void removeValue(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void invalidate() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isNew() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
