package io.honeybadger.reporter.dto;

import java.io.Serializable;

/**
 * Class representing an error that is reported to the Honeybadger API.
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.9
 */
public class ReportedError implements Serializable {
    private static final long serialVersionUID = 1661111694538362413L;

    private Notifier notifier = new Notifier();
    private ServerDetails server = new ServerDetails();
    private Details details = new Details();
    // This is defined as serializable so that it can use APIs that the
    // implementers may not have available like the Servlet API
    private Request request;
    private ErrorDetails error;

    public ReportedError() {
    }

    public Notifier getNotifier() {
        return notifier;
    }

    public ReportedError setNotifier(Notifier notifier) {
        this.notifier = notifier;
        return this;
    }

    public ErrorDetails getError() {
        return error;
    }

    public ReportedError setError(ErrorDetails error) {
        this.error = error;
        return this;
    }

    public ReportedError setServer(ServerDetails server) {
        this.server = server;
        return this;
    }

    public ServerDetails getServer() {
        return server;
    }

    public Details getDetails() {
        return details;
    }

    public ReportedError setDetails(Details details) {
        this.details = details;
        return this;
    }

    public Request getRequest() {
        return request;
    }

    public ReportedError setRequest(Request request) {
        this.request = request;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportedError that = (ReportedError) o;

        if (notifier != null ? !notifier.equals(that.notifier) : that.notifier != null) return false;
        if (server != null ? !server.equals(that.server) : that.server != null) return false;
        if (details != null ? !details.equals(that.details) : that.details != null) return false;
        if (request != null ? !request.equals(that.request) : that.request != null) return false;
        return !(error != null ? !error.equals(that.error) : that.error != null);

    }

    @Override
    public int hashCode() {
        int result = notifier != null ? notifier.hashCode() : 0;
        result = 31 * result + (server != null ? server.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        result = 31 * result + (request != null ? request.hashCode() : 0);
        result = 31 * result + (error != null ? error.hashCode() : 0);
        return result;
    }
}
