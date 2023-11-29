/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.web.protocol;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.shiro.authz.AuthorizationException;
import sonia.scm.NotFoundException;
import sonia.scm.PushStateDispatcher;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.WebElement;
import sonia.scm.repository.DefaultRepositoryProvider;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.spi.HttpScmProtocol;
import sonia.scm.security.Authentications;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.ScmClientDetector;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentParser;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Singleton
@WebElement(value = HttpProtocolServlet.PATTERN)
@Slf4j
public class HttpProtocolServlet extends HttpServlet {

  public static final String PATH = "/repo";
  public static final String PATTERN = PATH + "/*";

  private final ScmConfiguration configuration;
  private final RepositoryServiceFactory serviceFactory;
  private final NamespaceAndNameFromPathExtractor pathExtractor;
  private final PushStateDispatcher dispatcher;
  private final UserAgentParser userAgentParser;
  private final Set<ScmClientDetector> scmClientDetectors;

  @Inject
  public HttpProtocolServlet(ScmConfiguration configuration,
                             RepositoryServiceFactory serviceFactory,
                             NamespaceAndNameFromPathExtractor pathExtractor,
                             PushStateDispatcher dispatcher,
                             UserAgentParser userAgentParser,
                             Set<ScmClientDetector> scmClientDetectors) {
    this.configuration = configuration;
    this.serviceFactory = serviceFactory;
    this.pathExtractor = pathExtractor;
    this.dispatcher = dispatcher;
    this.userAgentParser = userAgentParser;
    this.scmClientDetectors = scmClientDetectors;
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    UserAgent userAgent = userAgentParser.parse(request);
    if (isScmClient(userAgent, request)) {
      String pathInfo = request.getPathInfo();
      Optional<NamespaceAndName> namespaceAndName = pathExtractor.fromUri(pathInfo);
      if (namespaceAndName.isPresent()) {
        service(request, response, namespaceAndName.get());
      } else {
        log.debug("namespace and name not found in request path {}", pathInfo);
        response.setStatus(HttpStatus.SC_BAD_REQUEST);
      }
    } else {
      log.trace("dispatch non-scm-client request for user agent {}", userAgent);
      dispatcher.dispatch(request, response, request.getRequestURI());
    }
  }

  private boolean isScmClient(UserAgent userAgent, HttpServletRequest request) {
    return userAgent.isScmClient() || scmClientDetectors.stream().anyMatch(detector -> detector.isScmClient(request, userAgent));
  }

  private void service(HttpServletRequest req, HttpServletResponse resp, NamespaceAndName namespaceAndName) throws IOException, ServletException {
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      req.setAttribute(DefaultRepositoryProvider.ATTRIBUTE_NAME, repositoryService.getRepository());
      HttpScmProtocol protocol = repositoryService.getProtocol(HttpScmProtocol.class);
      protocol.serve(req, resp, getServletConfig());
    } catch (NotFoundException e) {
      log.debug(e.getMessage());
      resp.setStatus(HttpStatus.SC_NOT_FOUND);
    } catch (AuthorizationException e) {
      log.debug(e.getMessage());
      if (Authentications.isAuthenticatedSubjectAnonymous()) {
        HttpUtil.sendUnauthorized(resp, configuration.getRealmDescription());
      } else {
        resp.setStatus(HttpStatus.SC_FORBIDDEN);
      }
    }
  }
}
