package sonia.scm;

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
    RequestDispatcher dispatcher = request.getRequestDispatcher("/index.html");
    try {
      dispatcher.forward(request, response);
    } catch (ServletException e) {
      throw new IOException("failed to forward request", e);
    }
  }
}
