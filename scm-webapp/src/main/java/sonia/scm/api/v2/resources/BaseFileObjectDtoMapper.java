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

  @VisibleForTesting
  void setResourceLinks(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  abstract SubRepositoryDto mapSubrepository(SubRepository subRepository);

  @ObjectFactory
  FileObjectDto createDto(@Context NamespaceAndName namespaceAndName, @Context BrowserResult browserResult, @Context Integer offset, FileObject fileObject) {
    String path = removeFirstSlash(fileObject.getPath());
    Links.Builder links = Links.linkingTo();
    if (fileObject.isDirectory()) {
      links.self(resourceLinks.source().sourceWithPath(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getRevision(), path));
    } else {
      links.self(resourceLinks.source().content(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getRevision(), path));
      links.single(link("history", resourceLinks.fileHistory().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getRevision(), path)));
      links.single(link("annotate", resourceLinks.annotate().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getRevision(), path)));
    }
    if (fileObject.isTruncated()) {
      links.single(link("proceed", resourceLinks.source().sourceWithPath(namespaceAndName.getNamespace(), namespaceAndName.getName(), browserResult.getRevision(), path) + "?offset=" + (offset + BrowseCommandRequest.DEFAULT_REQUEST_LIMIT)));
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
