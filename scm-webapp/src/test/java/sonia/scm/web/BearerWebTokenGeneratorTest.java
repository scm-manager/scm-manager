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
