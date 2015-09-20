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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoticeReportResult that = (NoticeReportResult) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (notice != null ? !notice.equals(that.notice) : that.notice != null) return false;
        return !(error != null ? !error.equals(that.error) : that.error != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (notice != null ? notice.hashCode() : 0);
        result = 31 * result + (error != null ? error.hashCode() : 0);
        return result;
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
