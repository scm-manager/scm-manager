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

import sonia.scm.cache.CacheManager;
import sonia.scm.user.UserDAO;

import javax.inject.Inject;

/**
 * Factory to create {@link DAORealmHelper} instances.
 * 
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class DAORealmHelperFactory {
  
  private final LoginAttemptHandler loginAttemptHandler;
  private final UserDAO userDAO; 
  private final CacheManager cacheManager;

  /**
   * Constructs a new instance.
   * @param loginAttemptHandler login attempt handler
   * @param userDAO user dao
   * @param cacheManager
   */
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
