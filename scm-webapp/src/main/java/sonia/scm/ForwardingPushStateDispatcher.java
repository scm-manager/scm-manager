package sonia.scm;

import sonia.scm.util.HttpUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This dispatcher forwards every request to the index.html of the application.
 *
 * @since 2.0.0
 */
public class ForwardingPushStateDispatcher implements PushStateDispatcher {
  @Override
  public void dispatch(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException {
    String path = HttpUtil.append(request.getContextPath(), "index.html");
    RequestDispatcher dispatcher = request.getRequestDispatcher(path);
    try {
      dispatcher.forward(request, response);
    } catch (ServletException e) {
      throw new IOException("failed to forward request", e);
    }
  }
}
