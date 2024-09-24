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
import sonia.scm.cli.CommandValidator;
import sonia.scm.cli.ParentCommand;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;

@CommandLine.Command(name = "list-permissions")
@ParentCommand(value = NamespaceCommand.class)
class NamespacePermissionsListCommand extends PermissionsListCommand<Namespace> {

  @CommandLine.Parameters(paramLabel = "namespace", index = "0", descriptionKey = "scm.namespace.list-permissions.namespace")
  private String namespace;

  @Inject
  public NamespacePermissionsListCommand(RepositoryTemplateRenderer templateRenderer, CommandValidator validator, NamespaceManager manager, RepositoryPermissionBeanMapper beanMapper) {
    super(templateRenderer, validator, new NamespacePermissionBaseAdapter(manager, templateRenderer), beanMapper);
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
