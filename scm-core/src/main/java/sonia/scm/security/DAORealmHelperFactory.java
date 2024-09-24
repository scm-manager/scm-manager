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

import jakarta.inject.Inject;
import sonia.scm.cache.CacheManager;
import sonia.scm.user.UserDAO;

/**
 * Factory to create {@link DAORealmHelper} instances.
 * 
 * @since 2.0.0
 */
public final class DAORealmHelperFactory {
  
  private final LoginAttemptHandler loginAttemptHandler;
  private final UserDAO userDAO; 
  private final CacheManager cacheManager;

  @Inject
  public DAORealmHelperFactory(LoginAttemptHandler loginAttemptHandler, UserDAO userDAO, CacheManager cacheManager) {
    this.loginAttemptHandler = loginAttemptHandler;
    this.userDAO = userDAO;
    this.cacheManager = cacheManager;
  }
  
  /**
   * Creates a new {@link DAORealmHelper} for the given realm with the injected dao instances.
   * 
   * @param realm name of realm
   * 
   * @return new {@link DAORealmHelper} instance.
   */
  public DAORealmHelper create(String realm) {
    return new DAORealmHelper(loginAttemptHandler, userDAO, realm);
  }
  
}
