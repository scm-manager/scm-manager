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
import sonia.scm.cli.ParentCommand;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRoleManager;

import javax.inject.Inject;

@CommandLine.Command(name = "set-role")
@ParentCommand(value = RepositoryCommand.class)
class RepositoryPermissionsSetRoleCommand extends RepositoryPermissionBaseCommand implements Runnable {

  private final RepositoryRoleManager roleManager;

  @CommandLine.Parameters(paramLabel = "namespace/name", index = "0", descriptionKey = "scm.repo.set-role.repository")
  private String repositoryName;
  @CommandLine.Parameters(paramLabel = "name", index = "1", descriptionKey = "scm.repo.set-role.name")
  private String name;
  @CommandLine.Parameters(paramLabel = "role", index = "2", descriptionKey = "scm.repo.set-role.role")
  private String role;

  @CommandLine.Option(names = {"--group", "-g"}, descriptionKey = "scm.repo.set-role.forGroup")
  private boolean forGroup;

  @Inject
  public RepositoryPermissionsSetRoleCommand(RepositoryManager repositoryManager, RepositoryRoleManager roleManager, RepositoryTemplateRenderer templateRenderer) {
    super(repositoryManager, roleManager, templateRenderer);
    this.roleManager = roleManager;
  }

  @Override
  public void run() {
    modifyRepository(
      repositoryName,
      repository -> {
        if (roleManager.get(role) == null) {
          renderRoleNotFoundError();
          return false;
        }
        replacePermission(repository, new RepositoryPermission(name, role, forGroup));
        return true;
      }
    );
  }

  @VisibleForTesting
  void setRepositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setRole(String role) {
    this.role = role;
  }

  public void setForGroup(boolean forGroup) {
    this.forGroup = forGroup;
  }
}
