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
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRoleManager;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Set;

import static java.util.Arrays.asList;

@CommandLine.Command(name = "add-permissions")
@ParentCommand(value = NamespaceCommand.class)
class NamespacePermissionsAddCommand extends NamespacePermissionBaseCommand implements Runnable {

  private final PermissionDescriptionResolver permissionDescriptionResolver;

  @CommandLine.Parameters(paramLabel = "namespace", index = "0", descriptionKey = "scm.namespace.add-permissions.namespace")
  private String namespace;
  @CommandLine.Parameters(paramLabel = "name", index = "1", descriptionKey = "scm.namespace.add-permissions.name")
  private String name;
  @CommandLine.Parameters(paramLabel = "verbs", index = "2..", arity = "1..", descriptionKey = "scm.namespace.add-permissions.verbs")
  private String[] verbs = new String[0];

  @CommandLine.Option(names = {"--group", "-g"}, descriptionKey = "scm.namespace.add-permissions.forGroup")
  private boolean forGroup;

  @Inject
  NamespacePermissionsAddCommand(NamespaceManager namespaceManager, RepositoryPermissionBaseCommand permissionCommandManager, RepositoryRoleManager roleManager, PermissionDescriptionResolver permissionDescriptionResolver, RepositoryTemplateRenderer templateRenderer) {
    super(namespaceManager, roleManager, templateRenderer);
    this.permissionDescriptionResolver = permissionDescriptionResolver;
  }

  @Override
  public void run() {
    modifyNamespace(
      namespace,
      ns -> {
        if (!Arrays.stream(verbs).allMatch(this::verifyVerbExists)) {
          return false;
        }
        Set<String> resultingVerbs =
          getPermissionsAsModifiableSet(ns, name, forGroup);
        if (resultingVerbs.containsAll(asList(this.verbs))) {
          return false;
        }
        resultingVerbs.addAll(asList(this.verbs));
        replacePermission(ns, new RepositoryPermission(name, resultingVerbs, forGroup));
        return true;
      }
    );
  }

  private boolean verifyVerbExists(String verb) {
    if (permissionDescriptionResolver.getDescription(verb).isEmpty()) {
      renderVerbNotFoundError();
      return false;
    }
    return true;
  }

  @VisibleForTesting
  void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setVerbs(String... verbs) {
    this.verbs = verbs;
  }

  public void setForGroup(boolean forGroup) {
    this.forGroup = forGroup;
  }
}
