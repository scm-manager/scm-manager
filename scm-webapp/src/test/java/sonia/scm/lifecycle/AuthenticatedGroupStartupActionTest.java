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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.group.GroupCollector.AUTHENTICATED;
import static sonia.scm.lifecycle.AuthenticatedGroupStartupAction.AUTHENTICATED_GROUP_DESCRIPTION;

@ExtendWith(MockitoExtension.class)
class AuthenticatedGroupStartupActionTest {

  @Mock
  private GroupManager groupManager;

  @InjectMocks
  private AuthenticatedGroupStartupAction startupAction;

  @Test
  void shouldCreateAuthenticatedGroupIfMissing() {
    when(groupManager.get(AUTHENTICATED)).thenReturn(null);

    startupAction.run();

    Group authenticated = createAuthenticatedGroup();
    authenticated.setDescription(AUTHENTICATED_GROUP_DESCRIPTION);
    authenticated.setExternal(true);

    verify(groupManager, times(1)).create(authenticated);
  }

  @Test
  void shouldNotCreateAuthenticatedGroupIfAlreadyExists() {
    when(groupManager.get(AUTHENTICATED)).thenReturn(createAuthenticatedGroup());

    startupAction.run();

    verify(groupManager, never()).create(any());
  }

  private Group createAuthenticatedGroup() {
    return new Group("xml", AUTHENTICATED);
  }
}
