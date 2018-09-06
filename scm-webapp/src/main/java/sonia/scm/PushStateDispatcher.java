package sonia.scm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The PushStateDispatcher is responsible for dispatching the request, to the main entry point of the ui, if no resource
 * could be found for the requested path. This allows us the implementation of a ui which work with "pushstate" of
 * html5.
 *
 * @since 2.0.0
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/History_API">HTML5 Push State</a>
 */
public interface PushStateDispatcher {

  /**
   * Dispatches the request to the main entry point of the ui.
   *
   * @param request http request
   * @param response http response
   * @param uri request uri
   *
   * @throws IOException
   */
  void dispatch(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException;

}
