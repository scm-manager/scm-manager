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

package sonia.scm.api.v2.resources;

import jakarta.inject.Inject;
import sonia.scm.repository.NamespaceAndName;

public class DefaultBranchLinkProvider implements BranchLinkProvider {

  private final ResourceLinks resourceLinks;

  @Inject
  public DefaultBranchLinkProvider(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  @Override
  public String get(NamespaceAndName namespaceAndName, String branch) {
    return resourceLinks.branch().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), branch);
  }
}
