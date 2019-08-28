package sonia.scm.web.filter;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentParser;
import sonia.scm.web.WebTokenGenerator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

public class HttpProtocolServletAuthenticationFilterBase extends AuthenticationFilter {

  private final UserAgentParser userAgentParser;

  protected HttpProtocolServletAuthenticationFilterBase(
    ScmConfiguration configuration,
    Set<WebTokenGenerator> tokenGenerators,
    UserAgentParser userAgentParser) {
    super(configuration, tokenGenerators);
    this.userAgentParser = userAgentParser;
  }

  @Override
  protected void handleUnauthorized(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    UserAgent userAgent = userAgentParser.parse(request);
    if (userAgent.isBrowser()) {
      // we can proceed the filter chain because the HttpProtocolServlet will render the ui if the client is a browser
      chain.doFilter(request, response);
    } else {
      HttpUtil.sendUnauthorized(request, response);
    }
  }
}
