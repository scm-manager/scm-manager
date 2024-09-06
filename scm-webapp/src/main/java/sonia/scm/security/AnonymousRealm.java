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
