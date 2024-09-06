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

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.NamespacePermissions;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SearchableType;
import sonia.scm.web.EdisonHalAppender;

import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Link.linkBuilder;
import static de.otto.edison.hal.Links.linkingTo;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class NamespaceToNamespaceDtoMapper extends BaseMapper<Namespace, NamespaceDto> {

  @Inject
  protected ResourceLinks links;
  @Inject
  protected SearchEngine searchEngine;
  @Inject
  protected NamespaceManager namespaceManager;

  public NamespaceDto map(String namespace) {
    return map(namespaceManager.get(namespace).orElseThrow(() -> notFound(entity(Namespace.class, namespace))));
  }

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract NamespaceDto map(Namespace namespace);

  @ObjectFactory
  NamespaceDto createDto(Namespace namespace) {
    Links.Builder linkingTo = linkingTo();
    linkingTo
      .self(links.namespace().self(namespace.getNamespace()))
      .single(link("repositories", links.repositoryCollection().forNamespace(namespace.getNamespace())));

    if (NamespacePermissions.permissionRead().isPermitted(namespace)) {
      linkingTo
        .single(link("permissions", links.namespacePermission().all(namespace.getNamespace())));
    }
    linkingTo.array(searchLinks(namespace.getNamespace()));
    linkingTo.single(link("searchableTypes", links.searchableTypes().searchableTypesForNamespace(namespace.getNamespace())));
    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linkingTo, embeddedBuilder), namespace, namespace.getNamespace());

    return new NamespaceDto(namespace.getNamespace(), linkingTo.build());
  }

  @VisibleForTesting
  void setLinks(ResourceLinks links) {
    this.links = links;
  }

  @VisibleForTesting
  void setSearchEngine(SearchEngine searchEngine) {
    this.searchEngine = searchEngine;
  }

  @VisibleForTesting
  void setNamespaceManager(NamespaceManager namespaceManager) {
    this.namespaceManager = namespaceManager;
  }

  private List<Link> searchLinks(String namespace) {
    return searchEngine.getSearchableTypes().stream()
      .filter(SearchableType::limitableToNamespace)
      .map(SearchableType::getName)
      .map(typeName ->
        linkBuilder("search", links.search().queryForNamespace(typeName, namespace)).withName(typeName).build()
      )
      .collect(Collectors.toList());
  }
}
