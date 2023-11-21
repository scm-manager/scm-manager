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

import com.google.common.io.BaseEncoding;
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

import javax.inject.Inject;
import javax.inject.Singleton;

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
