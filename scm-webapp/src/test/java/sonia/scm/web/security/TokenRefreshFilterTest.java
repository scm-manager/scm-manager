package sonia.scm.web.security;

import org.apache.shiro.authc.AuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.AccessTokenResolver;
import sonia.scm.security.BearerToken;
import sonia.scm.security.JwtAccessToken;
import sonia.scm.security.JwtAccessTokenRefresher;
import sonia.scm.web.WebTokenGenerator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class TokenRefreshFilterTest {

  @Mock
  private Set<WebTokenGenerator> tokenGenerators;
  @Mock
  private WebTokenGenerator tokenGenerator;
  @Mock
  private JwtAccessTokenRefresher refresher;
  @Mock
  private AccessTokenResolver resolver;
  @Mock
  private AccessTokenCookieIssuer issuer;

  @InjectMocks
  private TokenRefreshFilter filter;

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterChain filterChain;

  @BeforeEach
  void initGenerators() {
    when(tokenGenerators.iterator()).thenReturn(singleton(tokenGenerator).iterator());
  }

  @Test
  void shouldContinueChain() throws IOException, ServletException {
    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(issuer, never()).authenticate(any(), any(), any());
  }

  @Test
  void shouldNotRefreshNonBearerToken() throws IOException, ServletException {
    AuthenticationToken token = mock(AuthenticationToken.class);
    when(tokenGenerator.createToken(request)).thenReturn(token);

    filter.doFilter(request, response, filterChain);

    verify(issuer, never()).authenticate(any(), any(), any());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldNotRefreshNonJwtToken() throws IOException, ServletException {
    BearerToken token = mock(BearerToken.class);
    JwtAccessToken jwtToken = mock(JwtAccessToken.class);
    when(tokenGenerator.createToken(request)).thenReturn(token);
    when(resolver.resolve(token)).thenReturn(jwtToken);

    filter.doFilter(request, response, filterChain);

    verify(issuer, never()).authenticate(any(), any(), any());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldRefreshIfRefreshable() throws IOException, ServletException {
    BearerToken token = mock(BearerToken.class);
    JwtAccessToken jwtToken = mock(JwtAccessToken.class);
    JwtAccessToken newJwtToken = mock(JwtAccessToken.class);
    when(tokenGenerator.createToken(request)).thenReturn(token);
    when(resolver.resolve(token)).thenReturn(jwtToken);
    when(refresher.refresh(jwtToken)).thenReturn(of(newJwtToken));

    filter.doFilter(request, response, filterChain);

    verify(issuer).authenticate(request, response, newJwtToken);
    verify(filterChain).doFilter(request, response);
  }
}
