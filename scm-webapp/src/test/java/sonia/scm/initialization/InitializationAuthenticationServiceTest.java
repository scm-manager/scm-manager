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
