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
