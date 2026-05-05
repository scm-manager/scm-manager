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
    NumberedPaging paging = computeNumberedPaging(pageNumber, pageSize, pageResult);
    List<HalRepresentation> dtos = pageResult.getEntities().stream().map(mapper).collect(toList());
    Links links = createLinks(paging, selfLink, createLink, pageResult.isHasMore());
    Embedded embedded = embedDtos(dtos);
    CollectionDto collectionDto = createCollectionDto(links, embedded);
    collectionDto.setPage(pageNumber);
    collectionDto.setPageTotal(computePageTotal(pageSize, pageResult));
    return collectionDto;
  }

  CollectionDto createCollectionDto(Links links, Embedded embedded) {
    return new CollectionDto(links, embedded);
  }

  private NumberedPaging computeNumberedPaging(int pageNumber, int pageSize, PageResult<E> pageResult) {
    //If the pageNumber is negative, than the last page was requested
    //And since the last page was requested, the overall count should be always present anyway
    if (pageNumber < 0 && pageResult.getOverallCount().isPresent()) {
      int total = pageResult.getOverallCount().getAsInt();
      pageNumber = total % pageSize == 0 ? total / pageSize - 1 : total / pageSize;
    }

    if (pageResult.getOverallCount().isPresent()) {
      return zeroBasedNumberedPaging(pageNumber, pageSize, pageResult.getOverallCount().getAsInt());
    }

    return zeroBasedNumberedPaging(pageNumber, pageSize, pageResult.isHasMore());
  }

  private Integer computePageTotal(int pageSize, PageResult<E> pageResult) {
    if (pageResult.getOverallCount().isEmpty()) {
      return null;
    }

    int overallCount = pageResult.getOverallCount().getAsInt();
    if (overallCount % pageSize > 0) {
      return overallCount / pageSize + 1;
    } else {
      return overallCount / pageSize;
    }
  }

  private Links createLinks(NumberedPaging page, String selfLink, Optional<String> createLink, boolean hasMore) {
    Links.Builder linksBuilder = linkingTo()
      .with(page.links(
        fromTemplate(selfLink + "{?page,pageSize}"),
        EnumSet.allOf(PagingRel.class)));

    if (hasMore) {
      linksBuilder.single(link("last", String.format("%s?page=-1&pageSize=%d", selfLink, page.getPageSize())));
    }

    createLink.ifPresent(link -> linksBuilder.single(link("create", link)));
    return linksBuilder.build();
  }

  private Embedded embedDtos(List<HalRepresentation> dtos) {
    return embeddedBuilder()
      .with(collectionName, dtos)
      .build();
  }
}
