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
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissionHolder;
import sonia.scm.repository.RepositoryRoleManager;

import java.util.Set;

import static java.util.Arrays.asList;

abstract class PermissionsRemoveCommand<T extends RepositoryPermissionHolder> extends PermissionBaseCommand<T> implements Runnable {

  @CommandLine.Parameters(paramLabel = "name", index = "1", descriptionKey = "scm.repo.remove-permissions.name")
  private String name;
  @CommandLine.Parameters(paramLabel = "verbs", index = "2..", arity = "1..", descriptionKey = "scm.repo.remove-permissions.verbs")
  private String[] verbs = new String[0];

  @CommandLine.Option(names = {"--group", "-g"}, descriptionKey = "scm.repo.remove-permissions.forGroup")
  private boolean forGroup;

  PermissionsRemoveCommand(RepositoryRoleManager roleManager, RepositoryTemplateRenderer templateRenderer, PermissionBaseAdapter<T> adapter) {
    super(roleManager, templateRenderer, adapter);
  }

  @Override
  public void run() {
    modify(
      getIdentifier(),
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

  abstract String getIdentifier();

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setVerbs(String... verbs) {
    this.verbs = verbs;
  }

  @VisibleForTesting
  void setForGroup(boolean forGroup) {
    this.forGroup = forGroup;
  }
}
