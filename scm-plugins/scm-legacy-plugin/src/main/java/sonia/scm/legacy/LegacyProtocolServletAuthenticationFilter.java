package sonia.scm.legacy;

import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.web.UserAgentParser;
import sonia.scm.web.WebTokenGenerator;
import sonia.scm.web.filter.HttpProtocolServletAuthenticationFilterBase;

import javax.inject.Inject;
import java.util.Set;

@Priority(Filters.PRIORITY_AUTHENTICATION)
@WebElement(value = "/git/*", morePatterns = {"/hg/*", "/svn/*"})
public class LegacyProtocolServletAuthenticationFilter extends HttpProtocolServletAuthenticationFilterBase {

  @Inject
  public LegacyProtocolServletAuthenticationFilter(ScmConfiguration configuration, Set<WebTokenGenerator> tokenGenerators, UserAgentParser userAgentParser) {
    super(configuration, tokenGenerators, userAgentParser);
  }
}
