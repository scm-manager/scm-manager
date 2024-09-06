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

package sonia.scm;


import sonia.scm.security.CipherUtil;
import sonia.scm.user.User;
import sonia.scm.util.ServiceUtil;

/**
 * The SCMContext searches an implementation of {@link SCMContextProvider} and
 * holds a singleton instance of this implementation.
 *
 */
public final class SCMContext {

  /**
   * Default java package for finding extensions
   */
  public static final String DEFAULT_PACKAGE = "sonia.scm";

  /**
   * Name of the anonymous user
   */
  public static final String USER_ANONYMOUS = "_anonymous";

  /**
   * the anonymous user
   *
   * @since 1.21
   */
  public static final User ANONYMOUS = new User(
    USER_ANONYMOUS,
    "SCM Anonymous",
    "",
    CipherUtil.getInstance().encode("__not_necessary_password__"),
    "xml",
    true
  );

  /**
   * Singleton instance of {@link SCMContextProvider}
   */
  private static SCMContextProvider provider = null;


  private SCMContext() {
  }


  /**
   * Returns the singleton instance of {@link SCMContextProvider}
   */
  public static SCMContextProvider getContext() {
    synchronized (SCMContext.class) {
      if (provider == null) {
        provider = ServiceUtil.getService(SCMContextProvider.class);

        if (provider == null) {
          provider = new BasicContextProvider();
        }
      }
    }

    return provider;
  }
}
