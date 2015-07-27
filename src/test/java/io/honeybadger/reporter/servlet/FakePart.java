package io.honeybadger.reporter.servlet;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class FakePart implements Part {
    private final String name;
    private final String value;

    public FakePart(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSubmittedFileName() {
        return null;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public void write(String fileName) throws IOException {

    }

    @Override
    public void delete() throws IOException {

    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override
    public String toString() {
        return value;
    }
}
