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
import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryRoleManager;

@CommandLine.Command(name = "add-permissions")
@ParentCommand(value = RepositoryCommand.class)
class RepositoryPermissionsAddCommand extends PermissionsAddCommand<Repository> implements Runnable {

  @CommandLine.Parameters(paramLabel = "namespace/name", index = "0", descriptionKey = "scm.repo.add-permissions.repository")
  private String repositoryNamespaceAndName;

  @Inject
  RepositoryPermissionsAddCommand(RepositoryManager repositoryManager, RepositoryRoleManager roleManager, PermissionDescriptionResolver permissionDescriptionResolver, RepositoryTemplateRenderer templateRenderer) {
    super(roleManager, permissionDescriptionResolver, templateRenderer, new RepositoryPermissionBaseAdapter(repositoryManager, templateRenderer));
  }

  @Override
  String getIdentifier() {
    return repositoryNamespaceAndName;
  }

  @VisibleForTesting
  void setRepositoryNamespaceAndName(String repositoryNamespaceAndName) {
    this.repositoryNamespaceAndName = repositoryNamespaceAndName;
  }
}
