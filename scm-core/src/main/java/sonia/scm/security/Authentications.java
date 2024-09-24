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

import org.apache.shiro.SecurityUtils;
import sonia.scm.SCMContext;

public class Authentications {

  /**
   * Username of the system account.
   * @since 2.14.0
   */
  public static final String PRINCIPAL_SYSTEM = "_scmsystem";

  /**
   * Username of the anonymous account.
   * @since 2.14.0
   */
  public static final String PRINCIPAL_ANONYMOUS = SCMContext.USER_ANONYMOUS;

  private Authentications() {}

  public static boolean isAuthenticatedSubjectAnonymous() {
    return isSubjectAnonymous((String) SecurityUtils.getSubject().getPrincipal());
  }

  public static boolean isSubjectAnonymous(String principal) {
    return PRINCIPAL_ANONYMOUS.equals(principal);
  }

  /**
   * Returns true if the given principal is equal to the one from the system account.
   *
   * @param principal principal
   * @return {@code true}
   * @since 2.14.0
   */
  public static boolean isSubjectSystemAccount(String principal) {
    return PRINCIPAL_SYSTEM.equals(principal);
  }
}
