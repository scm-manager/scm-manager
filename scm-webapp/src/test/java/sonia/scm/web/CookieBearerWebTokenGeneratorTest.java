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

package sonia.scm.web;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.BearerToken;
import sonia.scm.security.SessionId;
import sonia.scm.util.HttpUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CookieBearerWebTokenGeneratorTest {

  private final CookieBearerWebTokenGenerator tokenGenerator = new CookieBearerWebTokenGenerator();

  @Mock
  private HttpServletRequest request;

  @Test
  void shouldCreateToken() {
    assignBearerCookie("value");

    BearerToken token = tokenGenerator.createToken(request);

    assertThat(token).isNotNull();
    assertThat(token.getPrincipal()).isNull();
    assertThat(token.getCredentials()).isEqualTo("value");
  }

  @Test
  void shouldCreateTokenWithSessionId() {
    when(request.getHeader(SessionId.PARAMETER)).thenReturn("abc123");

    assignBearerCookie("authc");

    BearerToken token = tokenGenerator.createToken(request);

    assertThat(token).isNotNull();
    assertThat(token.getPrincipal()).isEqualTo(SessionId.valueOf("abc123"));
    assertThat(token.getCredentials()).isEqualTo("authc");
  }

  private void assignBearerCookie(String value) {
    assignCookie(HttpUtil.COOKIE_BEARER_AUTHENTICATION, value);
  }

  private void assignCookie(String name, String value) {
    Cookie c = mock(Cookie.class);

    when(c.getName()).thenReturn(name);
    lenient().when(c.getValue()).thenReturn(value);
    when(request.getCookies()).thenReturn(new Cookie[]{c});
  }

  @Test
  void shouldNotCreateTokenForWrongCookie() {
    assignCookie("other-cookie", "with-some-value");

    BearerToken token = tokenGenerator.createToken(request);
    assertThat(token).isNull();
  }

  @Test
  void shouldNotCreateTokenWithoutCookies() {
    BearerToken token = tokenGenerator.createToken(request);
    assertThat(token).isNull();
  }
}
