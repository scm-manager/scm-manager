package sonia.scm.web.filter;

import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.web.UserAgentParser;
import sonia.scm.web.WebTokenGenerator;
import sonia.scm.web.protocol.HttpProtocolServlet;

import javax.inject.Inject;
import java.util.Set;

@Priority(Filters.PRIORITY_AUTHENTICATION)
@WebElement(value = HttpProtocolServlet.PATTERN)
public class DefaultHttpProtocolServletAuthenticationFilter extends HttpProtocolServletAuthenticationFilterBase {

  @Inject
  public DefaultHttpProtocolServletAuthenticationFilter(ScmConfiguration configuration, Set<WebTokenGenerator> tokenGenerators, UserAgentParser userAgentParser) {
    super(configuration, tokenGenerators, userAgentParser);
  }
}
