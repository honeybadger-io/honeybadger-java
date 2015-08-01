package io.honeybadger.reporter.dto;

import org.apache.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.LinkedHashMap;

/**
 * CGI parameters passed to the server when the error occurred.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class CgiData extends LinkedHashMap<String, Object> 
        implements Serializable {
    private static final long serialVersionUID = 1006793090880571738L;

    public CgiData() {
    }
}
