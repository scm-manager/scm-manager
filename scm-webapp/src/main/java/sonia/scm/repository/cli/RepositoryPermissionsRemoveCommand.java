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
import sonia.scm.repository.RepositoryPermission;

import javax.inject.Inject;
import java.util.Set;

import static java.util.Arrays.asList;

@CommandLine.Command(name = "remove-permissions")
@ParentCommand(value = RepositoryCommand.class)
class RepositoryPermissionsRemoveCommand implements Runnable {

  private final PermissionCommandManager permissionCommandManager;

  @CommandLine.Parameters(paramLabel = "namespace/name", index = "0", descriptionKey = "scm.repo.remove-permissions.repository")
  private String repositoryName;
  @CommandLine.Parameters(paramLabel = "name", index = "1", descriptionKey = "scm.repo.remove-permissions.name")
  private String name;
  @CommandLine.Parameters(paramLabel = "verbs", index = "2..", arity = "1..", descriptionKey = "scm.repo.remove-permissions.verbs")
  private String[] verbs = new String[0];

  @CommandLine.Option(names = {"--group", "-g"}, descriptionKey = "scm.repo.remove-permissions.forGroup")
  private boolean forGroup;

  @Inject
  public RepositoryPermissionsRemoveCommand(PermissionCommandManager permissionCommandManager) {
    this.permissionCommandManager = permissionCommandManager;
  }

  @Override
  public void run() {
    permissionCommandManager.modifyRepository(
      repositoryName,
      repository -> {
        Set<String> resultingVerbs =
          permissionCommandManager.getPermissionsAdModifiableSet(repository, name, forGroup);
        resultingVerbs.removeAll(asList(this.verbs));
        permissionCommandManager.replacePermission(repository, new RepositoryPermission(name, resultingVerbs, forGroup));
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

  public void setVerbs(String... verbs) {
    this.verbs = verbs;
  }

  public void setForGroup(boolean forGroup) {
    this.forGroup = forGroup;
  }
}
