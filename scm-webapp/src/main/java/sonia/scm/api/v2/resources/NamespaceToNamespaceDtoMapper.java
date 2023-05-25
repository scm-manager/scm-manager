/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.api.v2.resources;

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.NamespacePermissions;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SearchableType;
import sonia.scm.web.EdisonHalAppender;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Link.linkBuilder;
import static de.otto.edison.hal.Links.linkingTo;

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

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract NamespaceDto map(String namespace);

  @ObjectFactory
  NamespaceDto createDto(String namespace) {
    Links.Builder linkingTo = linkingTo();
    linkingTo
      .self(links.namespace().self(namespace))
      .single(link("repositories", links.repositoryCollection().forNamespace(namespace)));

    if (NamespacePermissions.permissionRead().isPermitted()) {
      linkingTo
        .single(link("permissions", links.namespacePermission().all(namespace)));
    }
    linkingTo.array(searchLinks(namespace));
    linkingTo.single(link("searchableTypes", links.searchableTypes().searchableTypesForNamespace(namespace)));
    Optional<Namespace> optionalNamespace = namespaceManager.get(namespace);
    if (optionalNamespace.isPresent()) {
      Embedded.Builder embeddedBuilder = embeddedBuilder();
      applyEnrichers(new EdisonHalAppender(linkingTo, embeddedBuilder), optionalNamespace.get(), namespace);
    }

    return new NamespaceDto(namespace, linkingTo.build());
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
