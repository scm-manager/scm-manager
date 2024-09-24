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

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;

import static java.util.Collections.singleton;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.security.BearerToken.valueOf;

@ExtendWith(MockitoExtension.class)
class ApiKeyRealmTest {

  @Mock
  ApiKeyService apiKeyService;
  @Mock
  DAORealmHelperFactory helperFactory;
  @Mock
  DAORealmHelper helper;
  @Mock(answer = Answers.RETURNS_SELF)
  DAORealmHelper.AuthenticationInfoBuilder authenticationInfoBuilder;
  @Mock
  RepositoryRoleManager repositoryRoleManager;
  @Mock
  ScmConfiguration scmConfiguration;

  ApiKeyRealm realm;

  @BeforeEach
  void initRealmHelper() {
    lenient().when(helperFactory.create("ApiTokenRealm")).thenReturn(helper);
    lenient().when(helper.authenticationInfoBuilder(any())).thenReturn(authenticationInfoBuilder);
    realm = new ApiKeyRealm(apiKeyService, helperFactory, repositoryRoleManager, scmConfiguration);
  }

  @Test
  void shouldCreateAuthenticationWithScope() {
    when(apiKeyService.check("towel")).thenReturn(of(new ApiKeyService.CheckResult("ford", "READ")));
    when(repositoryRoleManager.get("READ")).thenReturn(new RepositoryRole("guide", singleton("read"), "system"));

    realm.doGetAuthenticationInfo(valueOf("towel"));

    verify(helper).authenticationInfoBuilder("ford");
    verifyScopeSet("repository:read:*");
    verify(authenticationInfoBuilder).withSessionId(null);
  }

  @Test
  void shouldFailWithoutBearerToken() {
    AuthenticationToken otherToken = mock(AuthenticationToken.class);
    assertThrows(IllegalArgumentException.class, () -> realm.doGetAuthenticationInfo(otherToken));
  }

  @Test
  void shouldFailWithUnknownRole() {
    when(apiKeyService.check("towel")).thenReturn(of(new ApiKeyService.CheckResult("ford", "READ")));
    when(repositoryRoleManager.get("READ")).thenReturn(null);

    BearerToken token = valueOf("towel");
    assertThrows(AuthorizationException.class, () -> realm.doGetAuthenticationInfo(token));
  }

  @Test
  void shouldIgnoreTokensWithDots() {
    BearerToken token = valueOf("this.is.no.api.token");

    boolean supports = realm.supports(token);

    assertThat(supports).isFalse();
  }

  @Test
  void shouldIgnoreIfConfigIsDisabled() {
    when(scmConfiguration.isEnabledApiKeys()).thenReturn(false);
    AuthenticationToken token = mock(AuthenticationToken.class);

    boolean supports = realm.supports(token);

    assertThat(supports).isFalse();
  }

  @Test
  void shouldIgnoreNonBase64Tokens() {
    UsernamePasswordToken token = new UsernamePasswordToken("trillian", "My&SecretPassword");

    boolean supports = realm.supports(token);

    assertThat(supports).isFalse();
  }

  void verifyScopeSet(String... permissions) {
    verify(authenticationInfoBuilder).withScope(argThat(scope -> {
      assertThat(scope).containsExactly(permissions);
      return true;
    }));
  }
}
