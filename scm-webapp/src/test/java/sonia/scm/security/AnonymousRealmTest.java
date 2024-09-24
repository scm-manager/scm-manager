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

import jakarta.ws.rs.NotAuthorizedException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContext;
import sonia.scm.user.UserDAO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnonymousRealmTest {

  @Mock
  private DAORealmHelperFactory realmHelperFactory;

  @Mock
  private DAORealmHelper realmHelper;

  @Mock
  private DAORealmHelper.AuthenticationInfoBuilder builder;

  @Mock
  private UserDAO userDAO;

  private AnonymousRealm realm;

  @Mock
  private AuthenticationInfo authenticationInfo;

  @BeforeEach
  void prepareObjectUnderTest() {
    when(realmHelperFactory.create(AnonymousRealm.REALM)).thenReturn(realmHelper);
    realm = new AnonymousRealm(realmHelperFactory, userDAO);
  }

  @Test
  void shouldDoGetAuthentication() {
    when(realmHelper.authenticationInfoBuilder(SCMContext.USER_ANONYMOUS)).thenReturn(builder);
    when(builder.build()).thenReturn(authenticationInfo);
    when(userDAO.contains(SCMContext.USER_ANONYMOUS)).thenReturn(true);

    AuthenticationInfo result = realm.doGetAuthenticationInfo(new AnonymousToken());
    assertThat(result).isSameAs(authenticationInfo);
  }

  @Test
  void shouldThrowNotAuthorizedExceptionIfAnonymousUserNotExists() {
    when(userDAO.contains(SCMContext.USER_ANONYMOUS)).thenReturn(false);
    assertThrows(NotAuthorizedException.class, () -> realm.doGetAuthenticationInfo(new AnonymousToken()));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForWrongTypeOfToken() {
    when(userDAO.contains(SCMContext.USER_ANONYMOUS)).thenReturn(true);
    assertThrows(IllegalArgumentException.class, () -> realm.doGetAuthenticationInfo(new UsernamePasswordToken()));
  }
}
