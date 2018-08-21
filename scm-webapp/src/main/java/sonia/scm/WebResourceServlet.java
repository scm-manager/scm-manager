package sonia.scm;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.filter.WebElement;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.UberWebResourceLoader;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

/**
 * WebResourceServlet serves resources from the {@link UberWebResourceLoader}.
 *
 * @since 2.0.0
 */
@Singleton
@WebElement(value = WebResourceServlet.PATTERN, regex = true)
public class WebResourceServlet extends HttpServlet {

  /**
   * exclude api requests and the old frontend servlets.
   *
   * TODO remove old frontend servlets
   */
  @VisibleForTesting
  static final String PATTERN = "/(?!api/|index.html|error.html|plugins/resources).+";

  private static final Logger LOG = LoggerFactory.getLogger(WebResourceServlet.class);

  private final UberWebResourceLoader webResourceLoader;

  @Inject
  public WebResourceServlet(PluginLoader pluginLoader) {
    this.webResourceLoader = pluginLoader.getUberWebResourceLoader();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    String uri = normalizeUri(request);

    LOG.trace("try to load {}", uri);
    URL url = webResourceLoader.getResource(uri);
    if (url != null) {
      serveResource(response, url);
    } else {
      handleResourceNotFound(response);
    }
  }

  private String normalizeUri(HttpServletRequest request) {
    return HttpUtil.getStrippedURI(request);
  }

  private void serveResource(HttpServletResponse response, URL url) {
    // TODO lastModifiedDate, if-... ???
    try (OutputStream output = response.getOutputStream()) {
      Resources.copy(url, output);
    } catch (IOException ex) {
      LOG.warn("failed to serve resource: {}", url);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void handleResourceNotFound(HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
  }
}
