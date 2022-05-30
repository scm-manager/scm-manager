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

import org.apache.shiro.authc.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.KeyGenerator;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitializationAuthenticationServiceTest {

  private static final String INIT_TOKEN = "my_init_token";

  @Mock
  private KeyGenerator generator;
  @Mock
  private Provider<InitializationFinisher> initializationFinisherProvider;
  @Mock
  private InitializationFinisher initializationFinisher;
  @Mock
  private PermissionAssigner permissionAssigner;
  @Mock
  private AccessTokenCookieIssuer cookieIssuer;
  @Mock
  private InitializationCookieIssuer initializationCookieIssuer;
  @Mock
  private AdministrationContext administrationContext;

  @InjectMocks
  private InitializationAuthenticationService service;

  @Test
  void shouldInvalidateTokenIfInitializationFinished() {
    when(initializationFinisherProvider.get()).thenReturn(initializationFinisher);
    when(initializationFinisher.isFullyInitialized()).thenReturn(true);

    assertThrows(AuthenticationException.class, () -> service.validateToken("abcdef"));

    assertThat(service.getInitToken()).isNull();
  }

  @Test
  void shouldNotThrowExceptionIfTokenIsValid() {
    when(initializationFinisherProvider.get()).thenReturn(initializationFinisher);

    service.setInitToken(INIT_TOKEN);
    when(initializationFinisher.isFullyInitialized()).thenReturn(false);

    service.validateToken(INIT_TOKEN);
  }

  @Test
  void shouldSetPermissionForVirtualInitializationUserInAdminContext() {
    service.setPermissions();

    verify(administrationContext).runAsAdmin(any(PrivilegedAction.class));
  }

  @Test
  void shouldAuthenticate() {
    service.setInitToken(INIT_TOKEN);

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    service.authenticate(request, response);

    verify(initializationCookieIssuer).authenticateForInitialization(request, response, INIT_TOKEN);
  }

  @Test
  void shouldInvalidateCookies() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    service.invalidateCookies(request, response);

    verify(cookieIssuer).invalidate(request, response);
  }
}
