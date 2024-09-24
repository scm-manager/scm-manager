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
import sonia.scm.repository.RepositoryPermissionHolder;
import sonia.scm.repository.RepositoryRoleManager;

abstract class PermissionClearCommand<T extends RepositoryPermissionHolder> extends PermissionBaseCommand<T> implements Runnable {

  @CommandLine.Parameters(paramLabel = "name", index = "1", descriptionKey = "scm.repo.clear-permissions.name")
  private String name;
  @CommandLine.Option(names = {"--group", "-g"}, descriptionKey = "scm.repo.clear-permissions.forGroup")
  private boolean forGroup;

  PermissionClearCommand(RepositoryRoleManager roleManager, RepositoryTemplateRenderer templateRenderer, PermissionBaseAdapter<T> adapter) {
    super(roleManager, templateRenderer, adapter);
  }

  @Override
  public void run() {
    modify(
      getIdentifier(),
      ns -> {
        removeExistingPermission(ns, name, forGroup);
        return true;
      }
    );
  }

  abstract String getIdentifier();

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setForGroup(boolean forGroup) {
    this.forGroup = forGroup;
  }
}
