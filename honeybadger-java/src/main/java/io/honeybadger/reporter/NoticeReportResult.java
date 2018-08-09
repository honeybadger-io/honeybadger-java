package io.honeybadger.reporter;

import io.honeybadger.reporter.dto.Notice;

import java.util.Objects;
import java.util.UUID;

/**
 * Data object representing that results of an error submission to the
 * Honeybadger API.
 */
public class NoticeReportResult {
    private final UUID id;
    private final Notice notice;
    private final Throwable error;

    public NoticeReportResult(final UUID id, final Notice notice, final Throwable error) {
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoticeReportResult that = (NoticeReportResult) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(notice, that.notice) &&
                Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, notice, error);
    }

    @Override
    public String toString() {
        return "NoticeReportResult{" +
                "id=" + id +
                ", notice=" + notice +
                ", error=" + error +
                '}';
    }
}
