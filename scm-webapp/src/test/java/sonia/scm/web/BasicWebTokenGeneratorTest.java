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

/**
 * TODO add test with {@link UserAgentParser}.
 *
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
