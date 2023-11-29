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

package sonia.scm.initialization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitializationAuthenticationServiceTest {

  @Mock
  private AccessTokenBuilderFactory tokenBuilderFactory;
  @Mock(answer = Answers.RETURNS_SELF)
  private AccessTokenBuilder tokenBuilder;
  @Mock
  private AccessToken token;
  @Mock
  private AccessTokenCookieIssuer cookieIssuer;
  @Mock
  private InitializationCookieIssuer initializationCookieIssuer;
  @Mock
  private AdministrationContext administrationContext;

  @InjectMocks
  private InitializationAuthenticationService service;

  @Test
  void shouldNotThrowExceptionIfTokenIsValid() {
    when(token.getSubject()).thenReturn("SCM-INIT");

    service.validateToken(token);
  }

  @Test
  void shouldThrowExceptionIfTokenIsInvalid() {
    when(token.getSubject()).thenReturn("FAKE");

    assertThrows(AuthenticationException.class, () -> service.validateToken(token));
  }

  @Test
  void shouldSetPermissionForVirtualInitializationUserInAdminContext() {
    service.setPermissions();

    verify(administrationContext).runAsAdmin(any(PrivilegedAction.class));
  }

  @Test
  void shouldAuthenticate() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(tokenBuilderFactory.create()).thenReturn(tokenBuilder);
    AccessToken accessToken = mock(AccessToken.class);
    when(tokenBuilder.build()).thenReturn(accessToken);

    service.authenticate(request, response);

    verify(initializationCookieIssuer)
      .authenticateForInitialization(request, response, accessToken);
    verify(tokenBuilder).subject("SCM-INIT");
  }

  @Test
  void shouldInvalidateCookies() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    service.invalidateCookies(request, response);

    verify(cookieIssuer).invalidate(request, response);
  }
}
