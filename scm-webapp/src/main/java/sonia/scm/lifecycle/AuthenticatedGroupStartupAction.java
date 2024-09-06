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

package sonia.scm.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.plugin.Extension;

import static sonia.scm.group.GroupCollector.AUTHENTICATED;

@Extension
public class AuthenticatedGroupStartupAction implements PrivilegedStartupAction {

  @VisibleForTesting
  static final String AUTHENTICATED_GROUP_DESCRIPTION = "Includes all authenticated users";

  private final GroupManager groupManager;

  @Inject
  public AuthenticatedGroupStartupAction(GroupManager groupManager) {
    this.groupManager = groupManager;
  }

  @Override
  public void run() {
    if (authenticatedGroupDoesNotExists()) {
      createAuthenticatedGroup();
    }
  }

  private boolean authenticatedGroupDoesNotExists() {
    return groupManager.get(AUTHENTICATED) == null;
  }

  private void createAuthenticatedGroup() {
    Group authenticated = new Group("xml", AUTHENTICATED);
    authenticated.setDescription(AUTHENTICATED_GROUP_DESCRIPTION);
    authenticated.setExternal(true);
    groupManager.create(authenticated);
  }
}
