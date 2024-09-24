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
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.RepositoryRoleManager;

@CommandLine.Command(name = "add-permissions")
@ParentCommand(value = NamespaceCommand.class)
class NamespacePermissionsAddCommand extends PermissionsAddCommand<Namespace> implements Runnable {

  @CommandLine.Parameters(paramLabel = "namespace", index = "0", descriptionKey = "scm.namespace.add-permissions.namespace")
  private String namespace;

  @Inject
  NamespacePermissionsAddCommand(NamespaceManager namespaceManager, RepositoryRoleManager roleManager, PermissionDescriptionResolver permissionDescriptionResolver, RepositoryTemplateRenderer templateRenderer) {
    super(roleManager, permissionDescriptionResolver, templateRenderer, new NamespacePermissionBaseAdapter(namespaceManager, templateRenderer));
  }

  @Override
  String getIdentifier() {
    return namespace;
  }

  @VisibleForTesting
  void setNamespace(String namespace) {
    this.namespace = namespace;
  }
}
