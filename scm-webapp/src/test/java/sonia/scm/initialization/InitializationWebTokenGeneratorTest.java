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
