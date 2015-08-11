package io.honeybadger.reporter;

import io.honeybadger.reporter.dto.ReportedError;

import java.util.UUID;

/**
 * Data object representing that results of an error submission to the
 * Honeybadger API.
 */
public class ErrorReportResult {
    private final UUID id;
    private final ReportedError reportedError;
    private final Throwable error;

    public ErrorReportResult(UUID id, ReportedError reportedError, Throwable error) {
        this.id = id;
        this.reportedError = reportedError;
        this.error = error;
    }

    /**
     * @return Error ID as set by the Honeybadger API
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return Error DTO sent to the Honeybadger API
     */
    public ReportedError getReportedError() {
        return reportedError;
    }

    /**
     * @return Throwable that was used to create error DTO
     */
    public Throwable getError() {
        return error;
    }
}
