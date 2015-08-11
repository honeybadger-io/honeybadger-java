package io.honeybadger.reporter;

import io.honeybadger.reporter.dto.Notice;

import java.util.UUID;

/**
 * Data object representing that results of an error submission to the
 * Honeybadger API.
 */
public class NoticeReportResult {
    private final UUID id;
    private final Notice notice;
    private final Throwable error;

    public NoticeReportResult(UUID id, Notice notice, Throwable error) {
        this.id = id;
        this.notice = notice;
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
    public Notice getNotice() {
        return notice;
    }

    /**
     * @return Throwable that was used to create error DTO
     */
    public Throwable getError() {
        return error;
    }
}
