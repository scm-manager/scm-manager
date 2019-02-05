package sonia.scm.web.filter;

import sonia.scm.Priority;
import sonia.scm.PushStateDispatcher;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentParser;
import sonia.scm.web.WebTokenGenerator;
import sonia.scm.web.protocol.HttpProtocolServlet;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@Priority(Filters.PRIORITY_AUTHENTICATION)
@WebElement(value = HttpProtocolServlet.PATTERN)
public class HttpProtocolServletAuthenticationFilter extends AuthenticationFilter {

  private final PushStateDispatcher dispatcher;
  private final UserAgentParser userAgentParser;

  @Inject
  public HttpProtocolServletAuthenticationFilter(
    ScmConfiguration configuration,
    Set<WebTokenGenerator> tokenGenerators,
    PushStateDispatcher dispatcher,
    UserAgentParser userAgentParser) {
    super(configuration, tokenGenerators);
    this.dispatcher = dispatcher;
    this.userAgentParser = userAgentParser;
  }

  @Override
  protected void sendUnauthorizedError(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserAgent userAgent = userAgentParser.parse(request);
    if (userAgent.isBrowser()) {
      dispatcher.dispatch(request, response, request.getRequestURI());
    } else {
      HttpUtil.sendUnauthorized(request, response);
    }
  }
}
