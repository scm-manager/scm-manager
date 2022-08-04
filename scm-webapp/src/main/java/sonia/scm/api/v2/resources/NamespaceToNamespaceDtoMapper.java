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

import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import sonia.scm.repository.NamespacePermissions;
import sonia.scm.search.SearchEngine;
import sonia.scm.search.SearchableType;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Link.linkBuilder;
import static de.otto.edison.hal.Links.linkingTo;

class NamespaceToNamespaceDtoMapper {

  private final ResourceLinks links;
  private final SearchEngine searchEngine;

  @Inject
  NamespaceToNamespaceDtoMapper(ResourceLinks links, SearchEngine searchEngine) {
    this.links = links;
    this.searchEngine = searchEngine;
  }

  NamespaceDto map(String namespace) {
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
    return new NamespaceDto(namespace, linkingTo.build());
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
