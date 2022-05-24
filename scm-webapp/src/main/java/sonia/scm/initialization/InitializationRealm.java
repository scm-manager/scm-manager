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

package sonia.scm.initialization;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.subject.SimplePrincipalCollection;
import sonia.scm.plugin.Extension;
import sonia.scm.user.User;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;

@Extension
@Singleton
public class InitializationRealm extends AuthenticatingRealm {

  private static final String REALM = "InitializationRealm";
  public static final String INIT_PRINCIPAL = "__SCM_INIT__";

  private final InitializationAuthenticationService authenticationService;

  @Inject
  public InitializationRealm(InitializationAuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
    setAuthenticationTokenClass(InitializationToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    checkArgument(token instanceof InitializationToken, "%s is required", InitializationToken.class);
    authenticationService.validateToken((String) token.getCredentials());
    SimplePrincipalCollection principalCollection = new SimplePrincipalCollection(INIT_PRINCIPAL, REALM);
    principalCollection.add(new User(INIT_PRINCIPAL), REALM);
    authenticationService.setPermissions();
    return new SimpleAuthenticationInfo(principalCollection, token.getCredentials());
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof InitializationToken;
  }
}
