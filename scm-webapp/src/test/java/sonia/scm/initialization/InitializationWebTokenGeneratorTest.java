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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.initialization.InitializationWebTokenGenerator.INIT_TOKEN_HEADER;

@ExtendWith(MockitoExtension.class)
class InitializationWebTokenGeneratorTest {

  private static final String INIT_TOKEN = "my_init_token";
  private final InitializationWebTokenGenerator generator = new InitializationWebTokenGenerator();

  @Test
  void shouldReturnNullTokenIfCookieIsMissing() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    AuthenticationToken token = generator.createToken(request);

    assertThat(token).isNull();
  }

  @Test
  void shouldGenerateCookieToken() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getCookies()).thenReturn(new Cookie[]{new Cookie(INIT_TOKEN_HEADER, INIT_TOKEN)});

    AuthenticationToken token = generator.createToken(request);

    assertThat(token.getCredentials()).isEqualTo(INIT_TOKEN);
    assertThat(token.getPrincipal()).isEqualTo("SCM_INIT");
  }
}
