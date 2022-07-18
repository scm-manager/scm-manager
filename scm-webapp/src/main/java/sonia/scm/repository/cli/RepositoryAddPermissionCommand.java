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

@CommandLine.Command(name = "add-permission")
@ParentCommand(value = RepositoryCommand.class)
class RepositoryAddPermissionCommand implements Runnable {

  private final PermissionCommandManager permissionCommandManager;

  @CommandLine.Parameters(paramLabel = "namespace/name", index = "0", descriptionKey = "scm.repo.add-permission.repository")
   private String repositoryName;
  @CommandLine.Parameters(paramLabel = "name", index = "1", descriptionKey = "scm.repo.add-permission.name")
  private String name;
  @CommandLine.Parameters(paramLabel = "verb", index = "2", descriptionKey = "scm.repo.add-permission.verb")
  private String verb;

  @CommandLine.Option(names = {"--group", "-g"}, descriptionKey = "scm.repo.add-permission.forGroup")
  private boolean forGroup;

  @Inject
  RepositoryAddPermissionCommand(PermissionCommandManager permissionCommandManager) {
    this.permissionCommandManager = permissionCommandManager;
  }

  @Override
  public void run() {
    permissionCommandManager.modifyRepository(
      repositoryName,
      repository -> {
        Set<String> verbs =
          permissionCommandManager.getPermissionsAdModifiableSet(repository, name, forGroup);
        verbs.add(verb);
        permissionCommandManager.replacePermission(repository, new RepositoryPermission(name, verbs, forGroup));
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
  void setVerb(String verb) {
    this.verb = verb;
  }

  public void setForGroup(boolean forGroup) {
    this.forGroup = forGroup;
  }
}
