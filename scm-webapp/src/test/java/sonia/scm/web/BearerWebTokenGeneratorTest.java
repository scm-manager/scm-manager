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

import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.BearerToken;
import sonia.scm.security.SessionId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class BearerWebTokenGeneratorTest {

  private final BearerWebTokenGenerator tokenGenerator = new BearerWebTokenGenerator();

  @Mock
  private HttpServletRequest request;

  @Test
  void shouldNotCreateTokenWithWrongScheme() {
    when(request.getHeader("Authorization")).thenReturn("BASIC ASD");

    AuthenticationToken token = tokenGenerator.createToken(request);

    assertThat(token).isNull();
  }

  @Test
  void shouldNotCreateTokenWithoutAuthorizationHeader(){
    AuthenticationToken token = tokenGenerator.createToken(request);

    assertThat(token).isNull();
  }

  @Test
  void shouldCreateToken(){
    when(request.getHeader("Authorization")).thenReturn("Bearer asd");

    AuthenticationToken token = tokenGenerator.createToken(request);
    assertThat(token)
      .isNotNull()
      .isInstanceOf(BearerToken.class);

    BearerToken bt = (BearerToken) token;
    assertThat(bt.getCredentials()).isEqualTo("asd");
  }

  @Test
  void shouldCreateTokenWithSessionId(){
    doReturn("Bearer asd").when(request).getHeader("Authorization");
    doReturn("bcd123").when(request).getHeader(SessionId.PARAMETER);

    AuthenticationToken token = tokenGenerator.createToken(request);
    assertThat(token)
      .isNotNull()
      .isInstanceOf(BearerToken.class);

    BearerToken bt = (BearerToken) token;
    assertThat(bt.getPrincipal()).isEqualTo(SessionId.valueOf("bcd123"));
    assertThat(bt.getCredentials()).isEqualTo("asd");
  }

}
