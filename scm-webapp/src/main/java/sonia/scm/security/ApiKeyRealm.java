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
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.realm.AuthenticatingRealm;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
@Extension
public class ApiKeyRealm extends AuthenticatingRealm {

  private final ApiKeyService apiKeyService;
  private final DAORealmHelper helper;
  private final RepositoryRoleManager repositoryRoleManager;

  @Inject
  public ApiKeyRealm(ApiKeyService apiKeyService, DAORealmHelperFactory helperFactory, RepositoryRoleManager repositoryRoleManager) {
    this.apiKeyService = apiKeyService;
    this.helper = helperFactory.create("ApiTokenRealm");
    this.repositoryRoleManager = repositoryRoleManager;
    setAuthenticationTokenClass(BearerToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof UsernamePasswordToken || token instanceof BearerToken;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
    checkArgument(
      token instanceof BearerToken || token instanceof UsernamePasswordToken,
      "%s is required", BearerToken.class);
    String password = getPassword(token);
    ApiKeyService.CheckResult check = apiKeyService.check(password);
    RepositoryRole repositoryRole = repositoryRoleManager.get(check.getPermissionRole());
    if (repositoryRole == null) {
      throw new AuthorizationException("api key has unknown role: " + check.getPermissionRole());
    }
    String scope = "repository:" + String.join(",", repositoryRole.getVerbs()) + ":*";
    return helper
      .authenticationInfoBuilder(check.getUser())
      .withSessionId(getPrincipal(token))
      .withScope(Scope.valueOf(scope))
      .build();
  }

  private String getPassword(AuthenticationToken token) {
    if (token instanceof BearerToken) {
      return ((BearerToken) token).getCredentials();
    } else {
      return new String(((UsernamePasswordToken) token).getPassword());
    }
  }

  private SessionId getPrincipal(AuthenticationToken token) {
    if (token instanceof BearerToken) {
      return ((BearerToken) token).getPrincipal();
    } else {
      return SessionId.valueOf((((UsernamePasswordToken) token).getPrincipal()).toString());
    }
  }
}
