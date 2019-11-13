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

import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationToken;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.BearerToken;
import sonia.scm.security.SessionId;
import sonia.scm.util.HttpUtil;

/**
 *
 * @author Sebastian Sdorra
 */
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
    doReturn("bcd123").when(request).getHeader(HttpUtil.HEADER_SCM_SESSION);

    AuthenticationToken token = tokenGenerator.createToken(request);
    assertThat(token)
      .isNotNull()
      .isInstanceOf(BearerToken.class);

    BearerToken bt = (BearerToken) token;
    assertThat(bt.getPrincipal()).isEqualTo(SessionId.valueOf("bcd123"));
    assertThat(bt.getCredentials()).isEqualTo("asd");
  }

}
