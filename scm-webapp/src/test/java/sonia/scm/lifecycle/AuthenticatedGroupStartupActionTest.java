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
