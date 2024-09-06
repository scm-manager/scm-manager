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

package sonia.scm.user;


import sonia.scm.Manager;
import sonia.scm.search.Searchable;

import java.util.Collection;

/**
 * The central class for managing {@link User} objects.
 * This class is a singleton and is available via injection.
 *
 */
public interface UserManager
        extends Manager<User>, Searchable<User>
{

  /**
   * Returns true if a user with the specified username exists.
   */
  public boolean contains(String username);


  /**
   * Returns the default type for users.
   *
   *  @since 1.14
   */
  public String getDefaultType();

  default boolean isTypeDefault(User user) {
    return getDefaultType().equals(user.getType());
  }

  /**
   * Changes the password of the logged in user.
   * @param oldPassword The current encrypted password of the user.
   * @param newPassword The new encrypted password of the user.
   */
  void changePasswordForLoggedInUser(String oldPassword, String newPassword);

  /**
   * Overwrites the password for the given user id. This needs user write privileges.
   * @param userId The id of the user to change the password for.
   * @param newPassword The new encrypted password.
   */
  void overwritePassword(String userId, String newPassword);
}
