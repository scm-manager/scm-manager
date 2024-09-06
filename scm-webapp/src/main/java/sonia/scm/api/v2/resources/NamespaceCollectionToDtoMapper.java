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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import jakarta.inject.Inject;
import sonia.scm.repository.Namespace;

import java.util.Collection;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

public class NamespaceCollectionToDtoMapper {

  private final NamespaceToNamespaceDtoMapper namespaceMapper;
  private final ResourceLinks links;

  @Inject
  public NamespaceCollectionToDtoMapper(NamespaceToNamespaceDtoMapper namespaceMapper, ResourceLinks links) {
    this.namespaceMapper = namespaceMapper;
    this.links = links;
  }

  public HalRepresentation map(Collection<Namespace> namespaces) {
    Embedded namespaceDtos = embeddedBuilder()
      .with("namespaces", namespaces.stream().map(namespaceMapper::map).collect(toList()))
      .build();
    return new HalRepresentation(
      linkingTo().self(links.namespaceCollection().self()).build(),
      namespaceDtos
    );
  }
}
