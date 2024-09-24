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

import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;

import java.util.Optional;

import static java.util.Optional.empty;

class NamespacePermissionBaseAdapter implements PermissionBaseAdapter<Namespace> {

  private final NamespaceManager namespaceManager;
  private final RepositoryTemplateRenderer templateRenderer;

  NamespacePermissionBaseAdapter(NamespaceManager namespaceManager, RepositoryTemplateRenderer templateRenderer) {
    this.namespaceManager = namespaceManager;
    this.templateRenderer = templateRenderer;
  }

  @Override
  public Optional<Namespace> get(String namespace) {
    Optional<Namespace> ns = namespaceManager.get(namespace);
    if (ns.isPresent()) {
      return ns;
    } else {
      templateRenderer.renderNotFoundError();
      return empty();
    }
  }

  @Override
  public void modify(Namespace namespace) {
    namespaceManager.modify(namespace);
  }
}
