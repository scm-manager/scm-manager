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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.NotAuthorizedException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import sonia.scm.SCMContext;
import sonia.scm.plugin.Extension;
import sonia.scm.user.UserDAO;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
@Extension
public class AnonymousRealm extends AuthenticatingRealm {

  @VisibleForTesting
  static final String REALM = "AnonymousRealm";

  private final DAORealmHelper helper;
  private final UserDAO userDAO;

  @Inject
  public AnonymousRealm(DAORealmHelperFactory helperFactory, UserDAO userDAO) {
    this.helper = helperFactory.create(REALM);
    this.userDAO = userDAO;

    setAuthenticationTokenClass(AnonymousToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {
    if (!userDAO.contains(SCMContext.USER_ANONYMOUS)) {
     throw new NotAuthorizedException("trying to access anonymous but _anonymous user does not exist");
    }
    checkArgument(authenticationToken instanceof AnonymousToken, "%s is required", AnonymousToken.class);
    return helper.authenticationInfoBuilder(SCMContext.USER_ANONYMOUS).build();
  }
}
