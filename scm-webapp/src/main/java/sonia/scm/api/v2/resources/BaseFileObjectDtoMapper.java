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
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.Context;
import org.mapstruct.MapperConfig;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.SubRepository;
import sonia.scm.repository.spi.BrowseCommandRequest;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;

@MapperConfig
abstract class BaseFileObjectDtoMapper extends HalAppenderMapper implements InstantAttributeMapper {

  @Inject
  private ResourceLinks resourceLinks;
  @Inject
  private SourceLinkProvider sourceLinkProvider;

  @VisibleForTesting
  void setResourceLinks(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  @VisibleForTesting
  void setSourceLinkProvider(SourceLinkProvider sourceLinkProvider) {
    this.sourceLinkProvider = sourceLinkProvider;
  }

  abstract SubRepositoryDto mapSubrepository(SubRepository subRepository);

  @ObjectFactory
  FileObjectDto createDto(@Context NamespaceAndName namespaceAndName, @Context BrowserResult browserResult, @Context Integer offset, FileObject fileObject) {
    String path = removeFirstSlash(fileObject.getPath());
    Links.Builder links = Links.linkingTo();
    String selfLink = sourceLinkProvider.getSourceWithPath(namespaceAndName, browserResult.getRevision(), path);
    if (fileObject.isDirectory()) {
      links.self(selfLink);
    } else {
      links.self(resourceLinks.source().content(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getRevision(), path));
      links.single(link("history", resourceLinks.fileHistory().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getRevision(), path)));
      links.single(link("annotate", resourceLinks.annotate().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getRevision(), path)));
    }
    if (fileObject.isTruncated()) {
      links.single(link("proceed", selfLink + "?offset=" + (offset + BrowseCommandRequest.DEFAULT_REQUEST_LIMIT)));
    }

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(links, embeddedBuilder, namespaceAndName, browserResult, fileObject);

    return new FileObjectDto(links.build(), embeddedBuilder.build());
  }

  abstract void applyEnrichers(Links.Builder links, Embedded.Builder embeddedBuilder, NamespaceAndName namespaceAndName, BrowserResult browserResult, FileObject fileObject);

  private String removeFirstSlash(String source) {
    return source.startsWith("/") ? source.substring(1) : source;
  }

}
