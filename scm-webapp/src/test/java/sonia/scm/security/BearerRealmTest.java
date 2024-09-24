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

package sonia.scm.security;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.security.BearerToken.valueOf;

/**
 * Unit tests for {@link BearerRealm}.
 *
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

  @Test
  void shouldIgnoreTokensWithoutDot() {
    BearerToken token = valueOf("this-is-no-jwt-token");

    boolean supports = realm.supports(token);

    assertThat(supports).isFalse();
  }
}
