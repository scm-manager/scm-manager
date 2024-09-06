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


import sonia.scm.ManagerDecorator;
import sonia.scm.search.SearchRequest;

import java.util.Collection;

/**
 * Decorator for {@link UserManager}.
 *
 * @since 1.23
 */
public class UserManagerDecorator extends ManagerDecorator<User>
  implements UserManager
{
   private final UserManager decorated;

  public UserManagerDecorator(UserManager decorated)
  {
    super(decorated);
    this.decorated = decorated;
  }

  @Override
  public boolean contains(String username)
  {
    return decorated.contains(username);
  }

  @Override
  public Collection<User> search(SearchRequest searchRequest)
  {
    return decorated.search(searchRequest);
  }


  /**
   * Returns the decorated {@link UserManager}.
   *
   * @since 1.34
   */
  public UserManager getDecorated()
  {
    return decorated;
  }

  @Override
  public String getDefaultType()
  {
    return decorated.getDefaultType();
  }

  @Override
  public void changePasswordForLoggedInUser(String oldPassword, String newPassword) {
    decorated.changePasswordForLoggedInUser(oldPassword, newPassword);
  }

  @Override
  public void overwritePassword(String userId, String newPassword) {
    decorated.overwritePassword(userId, newPassword);
  }
}
