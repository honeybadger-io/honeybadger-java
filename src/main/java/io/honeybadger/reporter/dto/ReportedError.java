package io.honeybadger.reporter.dto;

import java.io.Serializable;

/**
 * Class representing an error that is reported to the Honeybadger API.
 */
public class ReportedError implements Serializable {
    private static final long serialVersionUID = 1661111694538362413L;

    private Notifier notifier = new Notifier();
    private ServerDetails server = new ServerDetails();
    private Details details = new Details();
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
}
