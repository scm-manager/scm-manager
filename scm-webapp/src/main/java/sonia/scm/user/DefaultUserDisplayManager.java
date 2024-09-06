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

import jakarta.inject.Inject;
import sonia.scm.GenericDisplayManager;
import sonia.scm.search.SearchRequest;
import sonia.scm.search.SearchUtil;

public class DefaultUserDisplayManager extends GenericDisplayManager<User, DisplayUser> implements UserDisplayManager {

  @Inject
  public DefaultUserDisplayManager(UserDAO userDAO) {
    super(userDAO, DisplayUser::from);
  }

  @Override
  protected void checkPermission() {
    UserPermissions.autocomplete().check();
  }

  @Override
  protected boolean matches(SearchRequest searchRequest, User user) {
    return SearchUtil.matchesOne(searchRequest, user.getName(), user.getDisplayName(), user.getMail());
  }
}
