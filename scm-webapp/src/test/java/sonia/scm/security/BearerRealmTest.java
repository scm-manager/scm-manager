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

package sonia.scm.security;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BearerRealm}.
 * 
 * @author Sebastian Sdorra
 */
@ExtendWith(MockitoExtension.class)
class BearerRealmTest {

  @Mock
  private DAORealmHelperFactory realmHelperFactory;

  @Mock
  private DAORealmHelper realmHelper;

  @Mock
  private AccessTokenResolver accessTokenResolver;

  @InjectMocks
  private BearerRealm realm;

  @Mock
  private AuthenticationInfo authenticationInfo;

  @BeforeEach
  void prepareObjectUnderTest() {
    when(realmHelperFactory.create(BearerRealm.REALM)).thenReturn(realmHelper);
    realm = new BearerRealm(realmHelperFactory, accessTokenResolver);
  }

  @Test
  void shouldDoGetAuthentication() {
    BearerToken bearerToken = BearerToken.valueOf("__bearer__");
    AccessToken accessToken = mock(AccessToken.class);
    when(accessToken.getSubject()).thenReturn("trillian");
    when(accessToken.getClaims()).thenReturn(new HashMap<>());

    when(accessTokenResolver.resolve(bearerToken)).thenReturn(accessToken);

    // we have to use answer, because we could not mock the result of Scopes
    when(realmHelper.getAuthenticationInfo(
      anyString(), anyString(), any(Scope.class)
    )).thenAnswer(createAnswer("trillian", "__bearer__", true));

    AuthenticationInfo result = realm.doGetAuthenticationInfo(bearerToken);
    assertThat(result).isSameAs(authenticationInfo);
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForWrongTypeOfToken() {
    assertThrows(IllegalArgumentException.class, () -> realm.doGetAuthenticationInfo(new UsernamePasswordToken()));
  }

  private Answer<AuthenticationInfo> createAnswer(String expectedSubject, String expectedCredentials, boolean scopeEmpty) {
    return (iom) -> {
      String subject = iom.getArgument(0);
      assertThat(subject).isEqualTo(expectedSubject);
      String credentials = iom.getArgument(1);
      assertThat(credentials).isEqualTo(expectedCredentials);
      Scope scope = iom.getArgument(2);
      assertThat(scope.isEmpty()).isEqualTo(scopeEmpty);

      return authenticationInfo;
    };
  }

  private class MyAnswer implements Answer<AuthenticationInfo> {

    @Override
    public AuthenticationInfo answer(InvocationOnMock invocationOnMock) throws Throwable {
      return null;
    }
  }
}
