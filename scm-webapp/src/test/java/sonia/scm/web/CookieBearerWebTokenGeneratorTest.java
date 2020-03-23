/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.BearerToken;
import sonia.scm.security.SessionId;
import sonia.scm.util.HttpUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
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
