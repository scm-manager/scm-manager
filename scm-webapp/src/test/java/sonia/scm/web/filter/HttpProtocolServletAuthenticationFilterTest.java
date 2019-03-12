package sonia.scm.web.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.Collections;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpProtocolServletAuthenticationFilterTest {

  private ScmConfiguration configuration = new ScmConfiguration();

  private Set<WebTokenGenerator> tokenGenerators = Collections.emptySet();

  @Mock
  private UserAgentParser userAgentParser;

  private HttpProtocolServletAuthenticationFilter authenticationFilter;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  private UserAgent nonBrowser = UserAgent.builder("i'm not a browser").browser(false).build();
  private UserAgent browser = UserAgent.builder("i am a browser").browser(true).build();

  @BeforeEach
  void setUpObjectUnderTest() {
    authenticationFilter = new HttpProtocolServletAuthenticationFilter(configuration, tokenGenerators, userAgentParser);
  }

  @Test
  void shouldSendUnauthorized() throws IOException, ServletException {
    when(userAgentParser.parse(request)).thenReturn(nonBrowser);

    authenticationFilter.handleUnauthorized(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, HttpUtil.STATUS_UNAUTHORIZED_MESSAGE);
  }

  @Test
  void shouldCallFilterChain() throws IOException, ServletException {
    when(userAgentParser.parse(request)).thenReturn(browser);

    authenticationFilter.handleUnauthorized(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

}
