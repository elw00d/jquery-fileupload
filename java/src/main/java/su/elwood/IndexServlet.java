package su.elwood;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: igor.kostromin
 * Date: 26.07.12
 * Time: 16:42
 */
public final class IndexServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // session must be created before upload will start
        req.getSession();
        //
        getServletConfig().getServletContext().getRequestDispatcher("/WEB-INF/index.jsp").forward(req, resp);
    }
}
