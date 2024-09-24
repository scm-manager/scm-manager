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

import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.security.RepositoryPermissionProvider;

@CommandLine.Command(name = "available-permissions")
@ParentCommand(value = RepositoryCommand.class)
class RepositoryPermissionsAvailableCommand extends PermissionsAvailableCommand {

  @Inject
  public RepositoryPermissionsAvailableCommand(RepositoryTemplateRenderer templateRenderer, RepositoryPermissionProvider repositoryPermissionProvider, PermissionDescriptionResolver permissionDescriptionResolver, RepositoryRoleManager repositoryRoleManager) {
    super(templateRenderer, repositoryPermissionProvider, permissionDescriptionResolver, repositoryRoleManager);
  }
}
