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

import com.google.common.io.BaseEncoding;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
@Extension
public class ApiKeyRealm extends AuthenticatingRealm {

  public static final String NAME = "ApiTokenRealm";

  private static final Logger LOG = LoggerFactory.getLogger(ApiKeyRealm.class);

  private final ApiKeyService apiKeyService;
  private final DAORealmHelper helper;
  private final RepositoryRoleManager repositoryRoleManager;
  private final ScmConfiguration scmConfiguration;

  @Inject
  public ApiKeyRealm(ApiKeyService apiKeyService, DAORealmHelperFactory helperFactory, RepositoryRoleManager repositoryRoleManager, ScmConfiguration scmConfiguration) {
    this.apiKeyService = apiKeyService;
    this.helper = helperFactory.create(NAME);
    this.repositoryRoleManager = repositoryRoleManager;
    this.scmConfiguration = scmConfiguration;
    setAuthenticationTokenClass(BearerToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  @SuppressWarnings("java:S4738") // java.util.Base64 has no canDecode method
  public boolean supports(AuthenticationToken token) {
    if (scmConfiguration.isEnabledApiKeys() && (token instanceof UsernamePasswordToken || token instanceof BearerToken)) {
      boolean isBase64 = BaseEncoding.base64().canDecode(getPassword(token));
      if (!isBase64) {
        LOG.debug("Ignoring non base 64 token; this is probably a JWT token or a normal password");
      }
      return isBase64;
    }
    return false;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
    checkArgument(
      token instanceof BearerToken || token instanceof UsernamePasswordToken,
      "%s is required", BearerToken.class);
    String password = getPassword(token);
    return apiKeyService.check(password)
      .map(check -> buildAuthenticationInfo(token, check))
      .orElse(null);
  }

  private AuthenticationInfo buildAuthenticationInfo(AuthenticationToken token, ApiKeyService.CheckResult check) {
    DAORealmHelper.AuthenticationInfoBuilder builder = helper
      .authenticationInfoBuilder(check.getUser())
      .withSessionId(getPrincipal(token));

    if (!check.getPermissionRole().equals("*")) {
      RepositoryRole repositoryRole = determineRole(check);
      Scope scope = createScope(repositoryRole);
      LOG.debug("login for user {} with api key limited to role {}", check.getUser(), check.getPermissionRole());
      builder = builder.withScope(scope);
    }

    return builder.build();
  }

  private String getPassword(AuthenticationToken token) {
    if (token instanceof BearerToken) {
      return ((BearerToken) token).getCredentials();
    } else {
      return new String(((UsernamePasswordToken) token).getPassword());
    }
  }

  private RepositoryRole determineRole(ApiKeyService.CheckResult check) {
    RepositoryRole repositoryRole = repositoryRoleManager.get(check.getPermissionRole());
    if (repositoryRole == null) {
      throw new AuthorizationException("api key has unknown role: " + check.getPermissionRole());
    }
    return repositoryRole;
  }

  private Scope createScope(RepositoryRole repositoryRole) {
    return Scope.valueOf("repository:" + String.join(",", repositoryRole.getVerbs()) + ":*");
  }

  private SessionId getPrincipal(AuthenticationToken token) {
    if (token instanceof BearerToken) {
      return ((BearerToken) token).getPrincipal();
    } else {
      return SessionId.valueOf((token.getPrincipal()).toString());
    }
  }
}
