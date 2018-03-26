package io.honeybadger.reporter.dto;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Request request = (Request) o;

        if (context != null ? !context.equals(request.context) : request.context != null) return false;
        if (url != null ? !url.equals(request.url) : request.url != null) return false;
        if (params != null ? !params.equals(request.params) : request.params != null) return false;
        if (session != null ? !session.equals(request.session) : request.session != null) return false;
        return !(cgi_data != null ? !cgi_data.equals(request.cgi_data) : request.cgi_data != null);

    }

    @Override
    public int hashCode() {
        int result = context != null ? context.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        result = 31 * result + (session != null ? session.hashCode() : 0);
        result = 31 * result + (cgi_data != null ? cgi_data.hashCode() : 0);
        return result;
    }
}
