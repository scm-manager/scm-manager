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
import sonia.scm.cli.CommandValidator;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRoleManager;

import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

@CommandLine.Command(name = "list-permissions")
@ParentCommand(value = RepositoryCommand.class)
class RepositoryPermissionsListCommand implements Runnable {

  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  @CommandLine.Mixin
  private final CommandValidator validator;
  private final RepositoryManager manager;
  private final RepositoryRoleManager roleManager;
  private final PermissionDescriptionResolver permissionDescriptionResolver;

  @CommandLine.Parameters(paramLabel = "namespace/name", index = "0", descriptionKey = "scm.repo.list-permissions.repository")
  private String repository;
  @CommandLine.Option(names = {"--verbose", "-v"}, descriptionKey = "scm.repo.list-permissions.verbose")
  private boolean verbose;
  @CommandLine.Option(names = {"--keys", "-k"}, descriptionKey = "scm.repo.list-permissions.keys")
  private boolean keys;

  @Inject
  public RepositoryPermissionsListCommand(RepositoryTemplateRenderer templateRenderer, CommandValidator validator, RepositoryManager manager, RepositoryRoleManager roleManager, PermissionDescriptionResolver permissionDescriptionResolver) {
    this.templateRenderer = templateRenderer;
    this.validator = validator;
    this.manager = manager;
    this.roleManager = roleManager;
    this.permissionDescriptionResolver = permissionDescriptionResolver;
  }

  @VisibleForTesting
  void setRepository(String repository) {
    this.repository = repository;
  }

  @VisibleForTesting
  void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public void setKeys(boolean keys) {
    this.keys = keys;
  }

  @Override
  public void run() {
    validator.validate();
    String[] splitRepo = repository.split("/");
    if (splitRepo.length == 2) {
      Repository repo = manager.get(new NamespaceAndName(splitRepo[0], splitRepo[1]));

      if (repo != null) {
        Collection<RepositoryPermissionBean> permissions =
          repo.getPermissions().stream().map(this::createPermissionBean).collect(Collectors.toList());
        if (verbose) {
          templateRenderer.renderVerbose(permissions);
        } else {
          templateRenderer.render(permissions);
        }
      } else {
        templateRenderer.renderNotFoundError();
      }
    } else {
      templateRenderer.renderInvalidInputError();
    }
  }

  private RepositoryPermissionBean createPermissionBean(RepositoryPermission permission) {
    Collection<String> effectiveVerbs;
    if (permission.getRole() == null) {
      effectiveVerbs = permission.getVerbs();
    } else {
      effectiveVerbs = roleManager.get(permission.getRole()).getVerbs();
    }
    return new RepositoryPermissionBean(
      permission.isGroupPermission(),
      permission.getName(),
      permission.getRole(),
      keys? effectiveVerbs: getDescriptions(effectiveVerbs)
    );
  }

  private Collection<String> getDescriptions(Collection<String> effectiveVerbs) {
    return effectiveVerbs.stream().map(this::getDescription).collect(Collectors.toList());
  }

  private String getDescription(String verb) {
    return permissionDescriptionResolver.getDescription(verb).orElse(verb);
  }
}
