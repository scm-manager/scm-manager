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
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRoleManager;

import javax.inject.Inject;
import java.util.Set;

import static java.util.Arrays.asList;

@CommandLine.Command(name = "remove-permissions")
@ParentCommand(value = NamespaceCommand.class)
class NamespacePermissionsRemoveCommand extends NamespacePermissionBaseCommand implements Runnable {

  @CommandLine.Parameters(paramLabel = "namespace", index = "0", descriptionKey = "scm.namespace.remove-permissions.namespace")
  private String namespace;
  @CommandLine.Parameters(paramLabel = "name", index = "1", descriptionKey = "scm.namespace.remove-permissions.name")
  private String name;
  @CommandLine.Parameters(paramLabel = "verbs", index = "2..", arity = "1..", descriptionKey = "scm.namespace.remove-permissions.verbs")
  private String[] verbs = new String[0];

  @CommandLine.Option(names = {"--group", "-g"}, descriptionKey = "scm.repo.remove-permissions.forGroup")
  private boolean forGroup;

  @Inject
  public NamespacePermissionsRemoveCommand(NamespaceManager namespaceManager, RepositoryRoleManager roleManager, RepositoryTemplateRenderer templateRenderer) {
    super(namespaceManager, roleManager, templateRenderer);
  }

  @Override
  public void run() {
    modify(
      namespace,
      ns -> {
        Set<String> resultingVerbs =
          getPermissionsAsModifiableSet(ns, name, forGroup);
        if (resultingVerbs.stream().noneMatch(verb -> asList(verbs).contains(verb))) {
          return false;
        }
        resultingVerbs.removeAll(asList(this.verbs));
        replacePermission(ns, new RepositoryPermission(name, resultingVerbs, forGroup));
        return true;
      }
    );
  }

  @VisibleForTesting
  void setNamespace(String namespace) {
    this.namespace = namespace;
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
