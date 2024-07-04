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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import de.otto.edison.hal.paging.NumberedPaging;
import de.otto.edison.hal.paging.PagingRel;
import sonia.scm.ModelObject;
import sonia.scm.PageResult;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static de.otto.edison.hal.paging.NumberedPaging.zeroBasedNumberedPaging;
import static java.util.stream.Collectors.toList;

abstract class PagedCollectionToDtoMapper<E extends ModelObject, D extends HalRepresentation> {

  private final String collectionName;

  PagedCollectionToDtoMapper(String collectionName) {
    this.collectionName = collectionName;
  }

  CollectionDto map(int pageNumber, int pageSize, PageResult<E> pageResult, String selfLink, Optional<String> createLink, Function<E, ? extends HalRepresentation> mapper) {
    NumberedPaging paging = zeroBasedNumberedPaging(pageNumber, pageSize, pageResult.getOverallCount());
    List<HalRepresentation> dtos = pageResult.getEntities().stream().map(mapper).collect(toList());
    Links links = createLinks(paging, selfLink, createLink);
    Embedded embedded = embedDtos(dtos);
    CollectionDto collectionDto = createCollectionDto(links, embedded);
    collectionDto.setPage(pageNumber);
    collectionDto.setPageTotal(computePageTotal(pageSize, pageResult));
    return collectionDto;
  }

  CollectionDto createCollectionDto(Links links, Embedded embedded) {
    return new CollectionDto(links, embedded);
  }

  private int computePageTotal(int pageSize, PageResult<E> pageResult) {
    if (pageResult.getOverallCount() % pageSize > 0) {
      return pageResult.getOverallCount() / pageSize + 1;
    } else {
      return pageResult.getOverallCount() / pageSize;
    }
  }

  private Links createLinks(NumberedPaging page, String selfLink, Optional<String> createLink) {
    Links.Builder linksBuilder = linkingTo()
      .with(page.links(
        fromTemplate(selfLink + "{?page,pageSize}"),
        EnumSet.allOf(PagingRel.class)));
    createLink.ifPresent(link -> linksBuilder.single(link("create", link)));
    return linksBuilder.build();
  }

  private Embedded embedDtos(List<HalRepresentation> dtos) {
    return embeddedBuilder()
      .with(collectionName, dtos)
      .build();
  }
}
