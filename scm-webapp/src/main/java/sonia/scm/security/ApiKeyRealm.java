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

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
@Extension
public class ApiKeyRealm extends AuthenticatingRealm {

  private final ApiKeyService apiKeyService;
  private final DAORealmHelper helper;

  @Inject
  public ApiKeyRealm(ApiKeyService apiKeyService, DAORealmHelperFactory helperFactory) {
    this.apiKeyService = apiKeyService;
    this.helper = helperFactory.create("ApiTokenRealm");
    setAuthenticationTokenClass(BearerToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
    checkArgument(token instanceof BearerToken, "%s is required", BearerToken.class);
    BearerToken bt = (BearerToken) token;
    ApiKeyService.CheckResult check = apiKeyService.check(bt.getCredentials());
    return helper
      .authenticationInfoBuilder(check.getUser())
      .withSessionId(bt.getPrincipal())
//      .withScope()
      .build();
  }
}
