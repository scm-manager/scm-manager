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
