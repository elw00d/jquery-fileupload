package su.elwood;

import org.apache.commons.fileupload.ProgressListener;

/**
 * User: igor.kostromin
 * Date: 26.07.12
 * Time: 17:02
 */
public final class UploadProgressListener implements ProgressListener {

    public final static String STATUS_IDLE = "idle";
    public final static String STATUS_PROGRESS = "progress";
    public final static String STATUS_UPLOADED = "uploaded";
    public final static String STATUS_ERROR = "error";

    private String status = STATUS_IDLE;
    private String errorMessage;

    private long portions = -1;
    private final static long PORTION_SIZE = 100 * 1024;   // 100 KB by default
    private long bytesReaded = 0;
    private long bytesTotal = 0;

    @Override
    public void update(long bytesRead, long contentLength, int itemIndex) {
        long _portions = bytesRead / PORTION_SIZE;
        if (status.equals(STATUS_IDLE)) {
            status = STATUS_PROGRESS;
        }
        if (portions == _portions) {
            if (bytesRead != contentLength) {
                return;
            }
        }
        portions = _portions;
        //
        this.bytesReaded = bytesRead;
        this.bytesTotal = contentLength;
        //
        if (!status.equals(STATUS_ERROR)) {
            if (bytesRead == contentLength) {
                status = STATUS_UPLOADED;
            } else if (!STATUS_PROGRESS.equals(status)) {
                status = STATUS_PROGRESS;
            }
        }
    }

    public long getBytesReaded() {
        return bytesReaded;
    }

    public void setBytesReaded(long bytesReaded) {
        this.bytesReaded = bytesReaded;
    }

    public long getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(long bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
