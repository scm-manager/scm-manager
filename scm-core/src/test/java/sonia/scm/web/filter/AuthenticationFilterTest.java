/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.web.filter;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.BearerToken;
import sonia.scm.web.WebTokenGenerator;

import java.io.IOException;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SubjectAware(configuration = "classpath:sonia/scm/shiro.ini")
public class AuthenticationFilterTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private FilterChain chain;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;

  private ScmConfiguration configuration;

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void testDoFilterAuthenticated() throws IOException, ServletException {
    AuthenticationFilter filter = createAuthenticationFilter();

    filter.doFilter(request, response, chain);
    verify(chain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
  }

  @Test
  public void testDoFilterUnauthorized() throws IOException, ServletException {
    AuthenticationFilter filter = createAuthenticationFilter();

    filter.doFilter(request, response, chain);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
  }

  @Test
  public void testDoFilterWithAuthenticationFailed() throws IOException, ServletException {
    AuthenticationFilter filter = createAuthenticationFilter(new DemoWebTokenGenerator("trillian", "sec"));

    filter.doFilter(request, response, chain);

    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
  }

  @Test
  public void testDoFilterWithAuthenticationSuccess() throws IOException, ServletException {
    AuthenticationFilter filter = createAuthenticationFilter();

    filter.doFilter(request, response, chain);

    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
  }

  @Test
  public void testExpiredBearerToken() throws IOException, ServletException {
    WebTokenGenerator generator = mock(WebTokenGenerator.class);
    when(generator.createToken(request)).thenReturn(BearerToken.create(null,
      "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzY21hZG1pbiIsImp0aSI6IjNqUzZ4TzMwUzEiLCJpYXQiOjE1OTY3ODA5Mjg"
        + "sImV4cCI6MTU5Njc0NDUyOCwic2NtLW1hbmFnZXIucmVmcmVzaEV4cGlyYXRpb24iOjE1OTY4MjQxMjg2MDIsInNjbS1tYW5h"
        + "Z2VyLnBhcmVudFRva2VuSWQiOiIzalM2eE8zMFMxIn0.utZLmzGZr-M6MP19yrd0dgLPkJ0u1xojwHKQi36_QAs"));
    AuthenticationFilter filter = createAuthenticationFilter(generator);

    filter.doFilter(request, response, chain);
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
  }

  @Before
  public void setUp() {
    configuration = new ScmConfiguration();
  }

  private AuthenticationFilter createAuthenticationFilter(WebTokenGenerator... generators) {
    return new AuthenticationFilter(configuration, ImmutableSet.copyOf(generators));
  }

  private static class DemoWebTokenGenerator implements WebTokenGenerator {

    private final String username;
    private final String password;

    public DemoWebTokenGenerator(String username, String password) {
      this.username = username;
      this.password = password;
    }

    @Override
    public AuthenticationToken createToken(HttpServletRequest request) {
      return new UsernamePasswordToken(username, password);
    }
  }


}
