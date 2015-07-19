package io.honeybadger.reporter.dto;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * Class representing the properties of an HTTP request that triggered an
 * error.
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 9105532956022860986L;

    public final Context context;
    public final String url;
    public final Params params;
    public final Session session;
    public final CgiData cgi_data;

    public Request(HttpServletRequest request) {
        this.context = new Context(request);
        this.url = getFullURL(request);
        this.params = new Params(request);
        this.session = new Session(request);
        this.cgi_data = new CgiData(request);
    }

    /** Gets the fully formed URL for a servlet request.
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
