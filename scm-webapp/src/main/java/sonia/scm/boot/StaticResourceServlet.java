package sonia.scm.boot;

import com.github.sdorra.webresources.CacheControl;
import com.github.sdorra.webresources.WebResourceSender;
import com.google.common.annotations.VisibleForTesting;
import sonia.scm.util.HttpUtil;

import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Singleton
public class StaticResourceServlet extends HttpServlet {

  private final WebResourceSender sender = WebResourceSender.create()
    .withGZIP()
    .withGZIPMinLength(512)
    .withBufferSize(16384)
    .withCacheControl(CacheControl.create().noCache());

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    URL resource = createResourceUrlFromRequest(request);
    if (resource != null) {
      sender.resource(resource).get(request, response);
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private URL createResourceUrlFromRequest(HttpServletRequest request) throws MalformedURLException {
    String uri = HttpUtil.getStrippedURI(request);
    return request.getServletContext().getResource(uri);
  }
}
