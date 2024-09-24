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
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissionHolder;
import sonia.scm.repository.RepositoryRoleManager;

import java.util.Arrays;
import java.util.Set;

import static java.util.Arrays.asList;

abstract class PermissionsAddCommand<T extends RepositoryPermissionHolder> extends PermissionBaseCommand<T> implements Runnable {

  private final PermissionDescriptionResolver permissionDescriptionResolver;

  @CommandLine.Parameters(paramLabel = "name", index = "1", descriptionKey = "scm.repo.add-permissions.name")
  private String name;
  @CommandLine.Parameters(paramLabel = "verbs", index = "2..", arity = "1..", descriptionKey = "scm.repo.add-permissions.verbs")
  private String[] verbs = new String[0];
  @CommandLine.Option(names = {"--group", "-g"}, descriptionKey = "scm.repo.add-permissions.forGroup")
  private boolean forGroup;

  PermissionsAddCommand(RepositoryRoleManager roleManager, PermissionDescriptionResolver permissionDescriptionResolver, RepositoryTemplateRenderer templateRenderer, PermissionBaseAdapter<T> adapter) {
    super(roleManager, templateRenderer, adapter);
    this.permissionDescriptionResolver = permissionDescriptionResolver;
  }

  abstract String getIdentifier();

  @Override
  public void run() {
    modify(
      getIdentifier(),
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
      renderVerbNotFoundError(verb);
      return false;
    }
    return true;
  }

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
