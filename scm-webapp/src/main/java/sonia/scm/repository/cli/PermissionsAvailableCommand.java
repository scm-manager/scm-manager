/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.cli;

import com.google.common.annotations.VisibleForTesting;
import picocli.CommandLine;
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.security.RepositoryPermissionProvider;

import java.util.List;

import static java.util.stream.Collectors.toList;

class PermissionsAvailableCommand implements Runnable {

  @CommandLine.Option(names = {"--roles", "-r"}, descriptionKey = "scm.repo.available-permissions.roles-only")
  private boolean roles;
  @CommandLine.Option(names = {"--verbs", "-v"}, descriptionKey = "scm.repo.available-permissions.verbs-only")
  private boolean verbs;

  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  private final RepositoryPermissionProvider repositoryPermissionProvider;
  private final PermissionDescriptionResolver permissionDescriptionResolver;
  private final RepositoryRoleManager repositoryRoleManager;

  public PermissionsAvailableCommand(RepositoryTemplateRenderer templateRenderer, RepositoryPermissionProvider repositoryPermissionProvider, PermissionDescriptionResolver permissionDescriptionResolver, RepositoryRoleManager repositoryRoleManager) {
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
