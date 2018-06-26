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

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static de.otto.edison.hal.paging.NumberedPaging.zeroBasedNumberedPaging;
import static java.util.stream.Collectors.toList;

abstract class BasicCollectionToDtoMapper<E extends ModelObject, D extends HalRepresentation> {

  private final String collectionName;
  private final BaseMapper<E, D> entityToDtoMapper;

  @Inject
  public BasicCollectionToDtoMapper(String collectionName, BaseMapper<E, D> entityToDtoMapper) {
    this.collectionName = collectionName;
    this.entityToDtoMapper = entityToDtoMapper;
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<E> pageResult) {
    NumberedPaging paging = zeroBasedNumberedPaging(pageNumber, pageSize, pageResult.getOverallCount());
    List<D> dtos = pageResult.getEntities().stream().map(entityToDtoMapper::map).collect(toList());

    CollectionDto collectionDto = new CollectionDto(
      createLinks(paging),
      embedDtos(dtos)
    );
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

  private Links createLinks(NumberedPaging page) {
    String baseUrl = createSelfLink();

    Links.Builder linksBuilder = linkingTo()
      .with(page.links(
        fromTemplate(baseUrl + "{?page,pageSize}"),
        EnumSet.allOf(PagingRel.class)));
    if (isCreatePermitted()) {
      linksBuilder.single(link("create", createCreateLink()));
    }
    return linksBuilder.build();
  }

  abstract boolean isCreatePermitted();

  abstract String createCreateLink();

  abstract String createSelfLink();

  private Embedded embedDtos(List<D> dtos) {
    return embeddedBuilder()
      .with(collectionName, dtos)
      .build();
  }
}
