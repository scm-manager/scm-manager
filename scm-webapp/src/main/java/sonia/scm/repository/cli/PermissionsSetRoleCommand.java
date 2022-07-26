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

package sonia.scm.repository.cli;

import com.google.common.annotations.VisibleForTesting;
import picocli.CommandLine;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissionHolder;
import sonia.scm.repository.RepositoryRoleManager;

abstract class PermissionsSetRoleCommand<T extends RepositoryPermissionHolder> extends PermissionBaseCommand<T> implements Runnable {

  private final RepositoryRoleManager roleManager;

  @CommandLine.Parameters(paramLabel = "name", index = "1", descriptionKey = "scm.repo.set-role.name")
  private String name;
  @CommandLine.Parameters(paramLabel = "role", index = "2", descriptionKey = "scm.repo.set-role.role")
  private String role;
  @CommandLine.Option(names = {"--group", "-g"}, descriptionKey = "scm.repo.set-role.forGroup")
  private boolean forGroup;

  PermissionsSetRoleCommand(RepositoryRoleManager roleManager, RepositoryTemplateRenderer templateRenderer, PermissionBaseAdapter<T> adapter) {
    super(roleManager, templateRenderer, adapter);
    this.roleManager = roleManager;
  }

  @Override
  public void run() {
    modify(
      getIdentifier(),
      ns -> {
        if (roleManager.get(role) == null) {
          renderRoleNotFoundError();
          return false;
        }
        replacePermission(ns, new RepositoryPermission(name, role, forGroup));
        return true;
      }
    );
  }

  protected abstract String getIdentifier();

  @VisibleForTesting
  void setRole(String role) {
    this.role = role;
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setForGroup(boolean forGroup) {
    this.forGroup = forGroup;
  }
}
