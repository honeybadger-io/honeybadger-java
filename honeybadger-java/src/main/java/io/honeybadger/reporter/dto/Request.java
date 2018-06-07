package io.honeybadger.reporter.dto;

import java.io.Serializable;
import java.util.Objects;

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

    public Request(final Context context, final String url,
                   final Params params, final Session session,
                   final CgiData cgiData) {
        this.context = context;
        this.url = url;
        this.params = params;
        this.session = session;
        this.cgi_data = cgiData;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(context, request.context) &&
                Objects.equals(url, request.url) &&
                Objects.equals(params, request.params) &&
                Objects.equals(session, request.session) &&
                Objects.equals(cgi_data, request.cgi_data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, url, params, session, cgi_data);
    }
}
