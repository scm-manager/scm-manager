package sonia.scm.web.protocol;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.PushStateDispatcher;
import sonia.scm.filter.WebElement;
import sonia.scm.repository.DefaultRepositoryProvider;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.spi.HttpScmProtocol;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
@WebElement(value = HttpProtocolServlet.PATTERN)
@Slf4j
public class HttpProtocolServlet extends HttpServlet {

  public static final String PATTERN = "/repo/*";

  private final RepositoryProvider repositoryProvider;
  private final RepositoryServiceFactory serviceFactory;

  private final Provider<HttpServletRequest> requestProvider;

  private final PushStateDispatcher dispatcher;
  private final UserAgentParser userAgentParser;

  @Inject
  public HttpProtocolServlet(RepositoryProvider repositoryProvider, RepositoryServiceFactory serviceFactory, Provider<HttpServletRequest> requestProvider, PushStateDispatcher dispatcher, UserAgentParser userAgentParser) {
    this.repositoryProvider = repositoryProvider;
    this.serviceFactory = serviceFactory;
    this.requestProvider = requestProvider;
    this.dispatcher = dispatcher;
    this.userAgentParser = userAgentParser;
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Subject subject = SecurityUtils.getSubject();


    UserAgent userAgent = userAgentParser.parse(req);
    if (userAgent.isBrowser()) {
      log.trace("dispatch browser request for user agent {}", userAgent);
      dispatcher.dispatch(req, resp, req.getRequestURI());
    } else {


      String pathInfo = req.getPathInfo();
      NamespaceAndName namespaceAndName = fromUri(pathInfo);
      try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
        requestProvider.get().setAttribute(DefaultRepositoryProvider.ATTRIBUTE_NAME, repositoryService.getRepository());
        HttpScmProtocol protocol = repositoryService.getProtocol(HttpScmProtocol.class);
        protocol.serve(req, resp);
      } catch (RepositoryNotFoundException e) {
        resp.setStatus(404);
      }
    }
  }

  private NamespaceAndName fromUri(String uri) {
    if (uri.startsWith(HttpUtil.SEPARATOR_PATH)) {
      uri = uri.substring(1);
    }

    String namespace = uri.substring(0, uri.indexOf(HttpUtil.SEPARATOR_PATH));
    String name = uri.substring(uri.indexOf(HttpUtil.SEPARATOR_PATH) + 1, uri.indexOf(HttpUtil.SEPARATOR_PATH, uri.indexOf(HttpUtil.SEPARATOR_PATH) + 1));

    return new NamespaceAndName(namespace, name);
  }
}
