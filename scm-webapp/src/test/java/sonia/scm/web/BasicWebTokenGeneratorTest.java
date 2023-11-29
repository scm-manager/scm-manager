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

//~--- non-JDK imports --------------------------------------------------------

import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.codec.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.BearerToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

/**
 * TODO add test with {@link UserAgentParser}.
 *
 * @author Sebastian Sdorra
 */
@ExtendWith(MockitoExtension.class)
class BasicWebTokenGeneratorTest {

  private BasicWebTokenGenerator generator;

  @Mock
  private HttpServletRequest request;

  @BeforeEach
  void setUpObjectUnderTest() {
    generator = new BasicWebTokenGenerator(null);
  }

  @Test
  void shouldCreateUsernamePasswordToken() {
    String trillian = Base64.encodeToString("trillian:secret".getBytes());

    when(request.getHeader("Authorization")).thenReturn("Basic ".concat(trillian));

    AuthenticationToken token = generator.createToken(request);
    assertThat(token)
      .isInstanceOfSatisfying(UsernamePasswordToken.class, usernamePasswordToken -> {
        assertThat(usernamePasswordToken.getPrincipal()).isEqualTo("trillian");
        assertThat(usernamePasswordToken.getPassword()).isEqualTo("secret".toCharArray());
      });
  }

  @Test
  void shouldCreateBearerToken() {
    String bearerToken = Base64.encodeToString(
      (BasicWebTokenGenerator.BEARER_TOKEN_IDENTIFIER + ":awesome_access_token").getBytes()
    );

    when(request.getHeader("Authorization")).thenReturn("Basic ".concat(bearerToken));

    assertThat(generator.createToken(request))
      .isInstanceOfSatisfying(
        BearerToken.class,
        token -> assertThat(token.getCredentials()).isEqualTo("awesome_access_token")
      );
  }

  @Test
  void shouldNotCreateTokenWithWrongAuthorizationHeader() {
    when(request.getHeader("Authorization")).thenReturn("NONBASIC ASD");

    AuthenticationToken token = generator.createToken(request);
    assertThat(token).isNull();
  }

  @Test
  void shouldNotCreateTokenWithWrongBasicAuthorizationHeader() {
    when(request.getHeader("Authorization")).thenReturn("Basic ASD");

    AuthenticationToken token = generator.createToken(request);
    assertThat(token).isNull();
  }

  @Test
  void testCreateTokenWithoutAuthorizationHeader() {
    AuthenticationToken token = generator.createToken(request);
    assertThat(token).isNull();
  }

  @Test
  void shouldNotCreateTokenWithoutPassword() {
    String trillian = Base64.encodeToString("trillian:".getBytes());
    when(request.getHeader("Authorization")).thenReturn("Basic ".concat(trillian));

    AuthenticationToken token = generator.createToken(request);
    assertThat(token).isNull();
  }

}
