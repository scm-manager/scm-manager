package sonia.scm;

import com.github.sdorra.webresources.CacheControl;
import com.github.sdorra.webresources.WebResourceSender;
import com.google.common.annotations.VisibleForTesting;
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
import java.net.URL;

/**
 * WebResourceServlet serves resources from the {@link UberWebResourceLoader}.
 *
 * @since 2.0.0
 */
@Singleton
@Priority(WebResourceServlet.PRIORITY)
@WebElement(value = WebResourceServlet.PATTERN, regex = true)
public class WebResourceServlet extends HttpServlet {


  /**
   * exclude api requests and the old frontend servlets.
   *
   * TODO remove old protocol servlets and hook. Move /hook/hg to api?
   */
  @VisibleForTesting
  static final String PATTERN = "/(?!api/|git/|hg/|svn/|hook/|repo/).*";

  // Be sure that this servlet is the last one in the servlet chain.
  static final int PRIORITY = Integer.MAX_VALUE;

  private static final Logger LOG = LoggerFactory.getLogger(WebResourceServlet.class);

  private final WebResourceSender sender = WebResourceSender.create()
    .withGZIP()
    .withGZIPMinLength(512)
    .withBufferSize(16384)
    .withCacheControl(CacheControl.create().noCache());

  private final UberWebResourceLoader webResourceLoader;
  private final PushStateDispatcher pushStateDispatcher;

  @Inject
  public WebResourceServlet(PluginLoader pluginLoader, PushStateDispatcher dispatcher) {
    this.webResourceLoader = pluginLoader.getUberWebResourceLoader();
    this.pushStateDispatcher = dispatcher;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    String uri = normalizeUri(request);

    LOG.trace("try to load {}", uri);
    URL url = webResourceLoader.getResource(uri);
    if (url != null) {
      LOG.trace("found {} -- serve as resource {}", uri, url);
      serveResource(request, response, url);
    } else {
      LOG.trace("could not find {} -- dispatch", uri);
      dispatch(request, response, uri);
    }
  }

  private void dispatch(HttpServletRequest request, HttpServletResponse response, String uri) {
    try {
      pushStateDispatcher.dispatch(request, response, uri);
    } catch (IOException ex) {
      LOG.error("failed to dispatch: " + uri, ex);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private String normalizeUri(HttpServletRequest request) {
    return HttpUtil.getStrippedURI(request);
  }

  private void serveResource(HttpServletRequest request, HttpServletResponse response, URL url) {
    try {
      LOG.debug("using sender to serve {}", request.getRequestURI());
      sender.resource(url).send(request, response);
    } catch (IOException ex) {
      LOG.warn("failed to serve resource: {}", url);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

}
