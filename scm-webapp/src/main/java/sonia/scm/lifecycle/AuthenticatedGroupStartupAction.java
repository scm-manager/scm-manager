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
