package sonia.scm.web.security;

import org.apache.shiro.authc.AuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Priority;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.AccessTokenResolver;
import sonia.scm.security.BearerToken;
import sonia.scm.security.JwtAccessToken;
import sonia.scm.security.JwtAccessTokenRefresher;
import sonia.scm.web.WebTokenGenerator;
import sonia.scm.web.filter.HttpFilter;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@Priority(Filters.PRIORITY_POST_AUTHENTICATION)
@WebElement(value = Filters.PATTERN_RESTAPI,
  morePatterns = { Filters.PATTERN_DEBUG })
public class TokenRefreshFilter extends HttpFilter {

  private static final Logger LOG = LoggerFactory.getLogger(TokenRefreshFilter.class);

  private final Set<WebTokenGenerator> tokenGenerators;
  private final AccessTokenCookieIssuer cookieIssuer;
  private final JwtAccessTokenRefresher refresher;
  private final AccessTokenResolver resolver;
  private final AccessTokenCookieIssuer issuer;

  @Inject
  public TokenRefreshFilter(Set<WebTokenGenerator> tokenGenerators, AccessTokenCookieIssuer cookieIssuer, JwtAccessTokenRefresher refresher, AccessTokenResolver resolver, AccessTokenCookieIssuer issuer) {
    this.tokenGenerators = tokenGenerators;
    this.cookieIssuer = cookieIssuer;
    this.refresher = refresher;
    this.resolver = resolver;
    this.issuer = issuer;
  }

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    AuthenticationToken token = createToken(request);
    if (token != null && token instanceof BearerToken) {
      AccessToken accessToken = resolver.resolve((BearerToken) token);
      if (accessToken instanceof JwtAccessToken) {
        refresher.refresh((JwtAccessToken) accessToken)
          .ifPresent(jwtAccessToken -> refreshToken(request, response, jwtAccessToken));
      }
    }
    chain.doFilter(request, response);
  }

  private void refreshToken(HttpServletRequest request, HttpServletResponse response, JwtAccessToken jwtAccessToken) {
    LOG.debug("refreshing authentication token");
    issuer.authenticate(request, response, jwtAccessToken);
  }

  private AuthenticationToken createToken(HttpServletRequest request) {
    for (WebTokenGenerator generator : tokenGenerators) {
      AuthenticationToken token = generator.createToken(request);
      if (token != null) {
        return token;
      }
    }
    return null;
  }
}
