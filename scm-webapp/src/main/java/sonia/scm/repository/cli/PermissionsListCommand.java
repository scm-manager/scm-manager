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
import sonia.scm.cli.CommandValidator;
import sonia.scm.repository.RepositoryPermissionHolder;

import java.util.Collection;
import java.util.Optional;

abstract class PermissionsListCommand<T extends RepositoryPermissionHolder> implements Runnable {

  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  @CommandLine.Mixin
  private final CommandValidator validator;
  private final PermissionBaseAdapter<T> adapter;
  private final RepositoryPermissionBeanMapper beanMapper;

  @CommandLine.Option(names = {"--verbose", "-v"}, descriptionKey = "scm.repo.list-permissions.verbose")
  private boolean verbose;
  @CommandLine.Option(names = {"--keys", "-k"}, descriptionKey = "scm.repo.list-permissions.keys")
  private boolean keys;

  PermissionsListCommand(RepositoryTemplateRenderer templateRenderer, CommandValidator validator, PermissionBaseAdapter<T> adapter, RepositoryPermissionBeanMapper beanMapper) {
    this.templateRenderer = templateRenderer;
    this.validator = validator;
    this.adapter = adapter;
    this.beanMapper = beanMapper;
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
    Optional<T> object = adapter.get(getIdentifier());
    if (object.isPresent()) {
      Collection<RepositoryPermissionBean> permissions = beanMapper.createPermissionBeans(object.get().getPermissions(), keys);
      if (verbose) {
        templateRenderer.renderVerbose(permissions);
      } else {
        templateRenderer.render(permissions);
      }
    }
  }

  abstract String getIdentifier();
}
