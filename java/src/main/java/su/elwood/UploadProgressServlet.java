package su.elwood;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * User: igor.kostromin
 * Date: 26.07.12
 * Time: 17:22
 */
public final class UploadProgressServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uploaderId = req.getParameter("id");
        if (uploaderId == null) {
            System.out.println("UploaderId parameter is not specified.");
            return;
        }
        UploadProgressListener progressListener = getProgressListener(req.getSession(), uploaderId);
        String jsonResult = null;
        if (null == progressListener) {
            // return idle status (if client side requested the listener that is not stored in session yet)
            jsonResult = "{\"bytesReaded\": 0, \"bytesTotal\": 0, \"status\": \"idle\", \"errorMessage\": \"null\"}";
        } else {
            jsonResult = String.format("{\"bytesReaded\": %d, \"bytesTotal\": %d, \"status\": \"%s\", \"errorMessage\": \"%s\"}",
                progressListener.getBytesReaded(), progressListener.getBytesTotal(),
                progressListener.getStatus(), progressListener.getErrorMessage());
        }
        resp.getWriter().write(jsonResult);
    }

    @SuppressWarnings({"unchecked"})
    private UploadProgressListener getProgressListener(HttpSession session, String uploaderId) {
        assert uploaderId != null;
        //
        Object progressListenersMapObject = session.getAttribute(UploadServletBase.PROGRESS_LISTENERS_REGISTRY_KEY);
        Map<String, UploadProgressListener> progressListenersMap;
        if (null == progressListenersMapObject) {
            System.out.println("Failed to get a listener for specified uploaderId.");
            return null;
        }
        progressListenersMap = (Map<String, UploadProgressListener>) progressListenersMapObject;
        if (!progressListenersMap.containsKey(uploaderId)) {
            return null;
        }
        return progressListenersMap.get(uploaderId);
    }
}
