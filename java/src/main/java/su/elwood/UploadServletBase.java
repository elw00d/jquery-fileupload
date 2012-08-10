package su.elwood;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: igor.kostromin
 * Date: 26.07.12
 * Time: 16:18
 */
public class UploadServletBase extends HttpServlet {
    
    public final static String PROGRESS_LISTENERS_REGISTRY_KEY = "PROGRESS_LISTENERS_REGISTRY";

    protected ServletFileUpload initializeFileUpload(String uploaderId, int maxFileSize, HttpSession session) {
        DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
        // set the size threshold, above which content will be stored on disk
        fileItemFactory.setSizeThreshold(maxFileSize);
        // set the temporary directory to store the uploaded files of size above threshold
        File tmpDir = (File) super.getServletContext().getAttribute("javax.servlet.context.tempdir");
        fileItemFactory.setRepository(tmpDir);
        //
        ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
        uploadHandler.setFileSizeMax(maxFileSize);
        //
        UploadProgressListener progressListener = createProgressListener(session, uploaderId);
        uploadHandler.setProgressListener(progressListener);
        return uploadHandler;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //
        String uploaderId = extractFileUploadId(req.getRequestURI());
        String timestamp = extractTimestamp(req.getRequestURI());
        int maxFileSize = 100 * 1000 * 1000;
        //
        ServletFileUpload uploadHandler = initializeFileUpload(uploaderId, maxFileSize, req.getSession());
        //
        try {
            List items = uploadHandler.parseRequest(req);
            for (Object item : items) {
                FileItem fileItem = (FileItem) item;
                // payload
            }
        } catch (Throwable e) {
            handleException(e, uploadHandler);
        } finally {
            resp.getWriter().write(getFrameHtmlCode(uploaderId, timestamp, uploadHandler));
        }
    }

    /**
     * Устанавливает статус ошибки с кастомным текстом.
     * Аналогично можно установить одну из стандартных ошибок, вызвав этот метод с одной
     * из предопределённых констант: internal_error, client_abort, size_limit_exceeded.
     */
    protected void handleCustomError(String errorMessage, ServletFileUpload uploadHandler) {
        UploadProgressListener progressListener = (UploadProgressListener) uploadHandler.getProgressListener();
        progressListener.setStatus("error");
        progressListener.setErrorMessage(escapeJson(errorMessage));
    }

    protected void handleException(Throwable e, ServletFileUpload uploadHandler) {
        UploadProgressListener progressListener = (UploadProgressListener) uploadHandler.getProgressListener();
        progressListener.setStatus("error");
        String message = getMessageForKnownException(e);
        if (message.equals("internal_error")) {
            // the exception is not well-known
            e.printStackTrace();
        }
        progressListener.setErrorMessage(message);
    }

    private String getMessageForKnownException(Throwable e) {
        assert e != null;

        // firstly check for ClientAbortException
        Throwable ee = e;
        while (ee != null) {
            if (ee.getClass().getName().contains("ClientAbortException")) {
                return "client_abort";
            }
            ee = ee.getCause();
        }

        if (e instanceof FileUploadBase.FileSizeLimitExceededException) {
            return "size_limit_exceeded";
        }

        if (e instanceof FileUploadBase.IOFileUploadException && e.getCause() != null &&
                e.getCause() instanceof MultipartStream.MalformedStreamException &&
                e.getCause().getMessage().equals("Stream ended unexpectedly")) {
            return "client_abort";
        }

        return "internal_error";
    }

    @SuppressWarnings({"unchecked"})
    private UploadProgressListener createProgressListener(HttpSession session, String uploaderId) {
        assert uploaderId != null;
        //
        Object progressListenersMapObject = session.getAttribute(PROGRESS_LISTENERS_REGISTRY_KEY);
        Map<String, UploadProgressListener> progressListenersMap;
        if (null == progressListenersMapObject) {
            progressListenersMap = new HashMap<String, UploadProgressListener>();
            session.setAttribute(PROGRESS_LISTENERS_REGISTRY_KEY, progressListenersMap);
        } else {
            progressListenersMap = (Map<String, UploadProgressListener>) progressListenersMapObject;
        }
        //
        UploadProgressListener progressListener = new UploadProgressListener();
        progressListenersMap.put(uploaderId, progressListener);
        return progressListener;
    }

    protected String extractFileUploadId(String requestUri) {
        String[] splitted = requestUri.split("/");
        String uploaderId = splitted[splitted.length - 1].split(":")[0];
        return uploaderId == null ? "" : uploaderId;
    }

    protected String extractTimestamp(String requestUri) {
        String[] splitted = requestUri.split("/");
        return splitted[splitted.length - 1].split(":")[1];
    }

    // returns iframe code which invokes the parent's javascript handler on frame loading
    protected String getFrameHtmlCode(String uploaderId, String timestamp, ServletFileUpload uploadHandler) {
        UploadProgressListener progressListener = (UploadProgressListener) uploadHandler.getProgressListener();
        //
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(String.format(
                "<body onload=\"window.setTimeout(function(){parent.jQuery.fileupload._uploadFinished('%s', '%s', %b, %s);},0);\">",
                uploaderId, timestamp, progressListener.getErrorMessage() != null,
                progressListener.getErrorMessage() != null ? "'" + progressListener.getErrorMessage() + "'" : "null"));
        sb.append("</body>");
        sb.append("</html>");
        //
        return sb.toString();
    }

    /**
     * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
     */
    public static String escapeJson(String s) {
        if (s == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        escapeJson(s, sb);
        return sb.toString();
    }

    static void escapeJson(String s, StringBuffer sb) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                default:
                    String ss = Integer.toHexString(ch);
                    sb.append("\\u");
                    for (int k = 0; k < 4 - ss.length(); k++) {
                        sb.append('0');
                    }
                    sb.append(ss.toUpperCase());
            }
        }
    }
}
