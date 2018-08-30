package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import de.otto.edison.hal.paging.NumberedPaging;
import de.otto.edison.hal.paging.PagingRel;
import sonia.scm.ModelObject;
import sonia.scm.PageResult;

import javax.inject.Inject;
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

abstract class BasicCollectionToDtoMapper<E extends ModelObject, D extends HalRepresentation, M extends BaseMapper<E, D>> {

  private final String collectionName;

  private final M entityToDtoMapper;

  @Inject
  public BasicCollectionToDtoMapper(String collectionName, M entityToDtoMapper) {
    this.collectionName = collectionName;
    this.entityToDtoMapper = entityToDtoMapper;
  }

  CollectionDto map(int pageNumber, int pageSize, PageResult<E> pageResult, String selfLink, Optional<String> createLink) {
    return map(pageNumber, pageSize, pageResult, selfLink, createLink, entityToDtoMapper::map);
  }

  CollectionDto map(int pageNumber, int pageSize, PageResult<E> pageResult, String selfLink, Optional<String> createLink, Function<E, ? extends HalRepresentation> mapper) {
    NumberedPaging paging = zeroBasedNumberedPaging(pageNumber, pageSize, pageResult.getOverallCount());
    List<HalRepresentation> dtos = pageResult.getEntities().stream().map(mapper).collect(toList());
    CollectionDto collectionDto = new CollectionDto(
      createLinks(paging, selfLink, createLink),
      embedDtos(dtos));
    collectionDto.setPage(pageNumber);
    collectionDto.setPageTotal(computePageTotal(pageSize, pageResult));
    return collectionDto;
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
