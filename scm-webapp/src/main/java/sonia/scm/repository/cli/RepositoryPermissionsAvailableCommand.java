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
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.security.RepositoryPermissionProvider;

import javax.inject.Inject;

import java.util.List;

import static java.util.stream.Collectors.toList;

@CommandLine.Command(name = "available-permissions")
@ParentCommand(value = RepositoryCommand.class)
class RepositoryPermissionsAvailableCommand implements Runnable {

  @CommandLine.Option(names = {"--roles", "-r"}, descriptionKey = "scm.repo.list-permissions.roles-only")
  private boolean roles;
  @CommandLine.Option(names = {"--verbs", "-v"}, descriptionKey = "scm.repo.list-permissions.verbs-only")
  private boolean verbs;

  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  private final RepositoryPermissionProvider repositoryPermissionProvider;
  private final PermissionDescriptionResolver permissionDescriptionResolver;
  private final RepositoryRoleManager repositoryRoleManager;

  @Inject
  public RepositoryPermissionsAvailableCommand(RepositoryTemplateRenderer templateRenderer, RepositoryPermissionProvider repositoryPermissionProvider, PermissionDescriptionResolver permissionDescriptionResolver, RepositoryRoleManager repositoryRoleManager) {
    this.templateRenderer = templateRenderer;
    this.repositoryPermissionProvider = repositoryPermissionProvider;
    this.permissionDescriptionResolver = permissionDescriptionResolver;
    this.repositoryRoleManager = repositoryRoleManager;
  }

  @VisibleForTesting
  void setRoles(boolean roles) {
    this.roles = roles;
  }

  @VisibleForTesting
  void setVerbs(boolean verbs) {
    this.verbs = verbs;
  }

  @Override
  public void run() {
    if (roles) {
      templateRenderer.renderRoles(getRoleBeans());
    } else if (verbs) {
      templateRenderer.renderVerbs(getVerbBeans());
    } else {
      templateRenderer.render(getRoleBeans(), getVerbBeans());
    }
  }

  private List<VerbBean> getVerbBeans() {
    return repositoryPermissionProvider.availableVerbs().stream().map(this::createBean).collect(toList());
  }

  private List<RoleBean> getRoleBeans() {
    return repositoryRoleManager.getAll().stream().map(this::createBean).collect(toList());
  }

  private VerbBean createBean(String verb) {
    return new VerbBean(verb, permissionDescriptionResolver.getDescription(verb).orElse(verb));
  }

  private RoleBean createBean(RepositoryRole role) {
    return new RoleBean(role.getName(), role.getVerbs());
  }
}
