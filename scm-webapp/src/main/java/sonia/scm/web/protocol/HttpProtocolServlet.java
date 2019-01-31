package sonia.scm.web.protocol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.shiro.authz.AuthorizationException;
import sonia.scm.NotFoundException;
import sonia.scm.PushStateDispatcher;
import sonia.scm.filter.WebElement;
import sonia.scm.repository.DefaultRepositoryProvider;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.spi.HttpScmProtocol;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentParser;

import javax.inject.Provider;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Singleton
@WebElement(value = HttpProtocolServlet.PATTERN)
@Slf4j
public class HttpProtocolServlet extends HttpServlet {

  public static final String PATH = "/repo";
  public static final String PATTERN = PATH + "/*";

  private final RepositoryServiceFactory serviceFactory;

  private final Provider<HttpServletRequest> requestProvider;

  private final PushStateDispatcher dispatcher;
  private final UserAgentParser userAgentParser;


  @Inject
  public HttpProtocolServlet(RepositoryServiceFactory serviceFactory, Provider<HttpServletRequest> requestProvider, PushStateDispatcher dispatcher, UserAgentParser userAgentParser) {
    this.serviceFactory = serviceFactory;
    this.requestProvider = requestProvider;
    this.dispatcher = dispatcher;
    this.userAgentParser = userAgentParser;
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    UserAgent userAgent = userAgentParser.parse(request);
    if (userAgent.isBrowser()) {
      log.trace("dispatch browser request for user agent {}", userAgent);
      dispatcher.dispatch(request, response, request.getRequestURI());
    } else {

      String pathInfo = request.getPathInfo();
      Optional<NamespaceAndName> namespaceAndName = NamespaceAndNameFromPathExtractor.fromUri(pathInfo);
      if (namespaceAndName.isPresent()) {
        service(request, response, namespaceAndName.get());
      } else {
        log.debug("namespace and name not found in request path {}", pathInfo);
        response.setStatus(HttpStatus.SC_BAD_REQUEST);
      }
    }
  }

  private void service(HttpServletRequest req, HttpServletResponse resp, NamespaceAndName namespaceAndName) throws IOException, ServletException {
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      requestProvider.get().setAttribute(DefaultRepositoryProvider.ATTRIBUTE_NAME, repositoryService.getRepository());
      HttpScmProtocol protocol = repositoryService.getProtocol(HttpScmProtocol.class);
      protocol.serve(req, resp, getServletConfig());
    } catch (NotFoundException e) {
      log.debug(e.getMessage());
      resp.setStatus(HttpStatus.SC_NOT_FOUND);
    } catch (AuthorizationException e) {
      log.debug(e.getMessage());
      resp.setStatus(HttpStatus.SC_FORBIDDEN);
    }
  }
}
