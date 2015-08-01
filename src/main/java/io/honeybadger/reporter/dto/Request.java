package io.honeybadger.reporter.dto;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * Class representing the properties of an HTTP request that triggered an
 * error.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 9105532956022860986L;

    public final Context context;
    public final String url;
    public final Params params;
    public final Session session;
    public final CgiData cgi_data;

    public Request(Context context, String url,
                   Params params, Session session,
                   CgiData cgi_data) {
        this.context = context;
        this.url = url;
        this.params = params;
        this.session = session;
        this.cgi_data = cgi_data;
    }
}
