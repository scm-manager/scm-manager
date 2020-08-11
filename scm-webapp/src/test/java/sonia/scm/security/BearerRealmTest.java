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

package sonia.scm.security;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
  private DAORealmHelper.AuthenticationInfoBuilder builder;

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
    BearerToken bearerToken = BearerToken.create(SessionId.valueOf("__session__"), "__bearer__");
    AccessToken accessToken = mock(AccessToken.class);

    when(accessToken.getSubject()).thenReturn("trillian");
    when(accessToken.getClaims()).thenReturn(new HashMap<>());
    when(accessTokenResolver.resolve(bearerToken)).thenReturn(accessToken);

    when(realmHelper.authenticationInfoBuilder("trillian")).thenReturn(builder);
    when(builder.withCredentials("__bearer__")).thenReturn(builder);
    when(builder.withScope(any(Scope.class))).thenReturn(builder);
    when(builder.withSessionId(any(SessionId.class))).thenReturn(builder);
    when(builder.build()).thenReturn(authenticationInfo);

    AuthenticationInfo result = realm.doGetAuthenticationInfo(bearerToken);
    assertThat(result).isSameAs(authenticationInfo);
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForWrongTypeOfToken() {
    assertThrows(IllegalArgumentException.class, () -> realm.doGetAuthenticationInfo(new UsernamePasswordToken()));
  }
}
