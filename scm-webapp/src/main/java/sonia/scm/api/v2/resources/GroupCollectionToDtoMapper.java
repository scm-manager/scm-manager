package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import de.otto.edison.hal.paging.NumberedPaging;
import de.otto.edison.hal.paging.PagingRel;
import sonia.scm.PageResult;
import sonia.scm.group.Group;
import sonia.scm.group.GroupPermissions;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static de.otto.edison.hal.paging.NumberedPaging.zeroBasedNumberedPaging;
import static sonia.scm.api.v2.resources.ResourceLinks.groupCollection;

public class GroupCollectionToDtoMapper {

  private final GroupToGroupDtoMapper groupToDtoMapper;
  private final UriInfoStore uriInfoStore;

  @Inject
  public GroupCollectionToDtoMapper(GroupToGroupDtoMapper groupToDtoMapper, UriInfoStore uriInfoStore) {
    this.groupToDtoMapper = groupToDtoMapper;
    this.uriInfoStore = uriInfoStore;
  }

  public GroupCollectionDto map(int pageNumber, int pageSize, PageResult<Group> pageResult) {
    NumberedPaging paging = zeroBasedNumberedPaging(pageNumber, pageSize, pageResult.hasMore());
    List<GroupDto> dtos = pageResult.getEntities().stream().map(user -> groupToDtoMapper.map(user, uriInfoStore.get())).collect(Collectors.toList());

    GroupCollectionDto groupCollectionDto = new GroupCollectionDto(
      createLinks(paging),
      embedDtos(dtos)
    );
    groupCollectionDto.setPage(pageNumber);
    return groupCollectionDto;
  }

  private Links createLinks(NumberedPaging page) {
    String baseUrl = groupCollection(uriInfoStore.get()).self();

    Links.Builder linksBuilder = linkingTo()
      .with(page.links(
        fromTemplate(baseUrl + "{?page,pageSize}"),
        EnumSet.allOf(PagingRel.class)));
    if (GroupPermissions.create().isPermitted()) {
      linksBuilder.single(link("create", groupCollection(uriInfoStore.get()).create()));
    }
    return linksBuilder.build();
  }

  private Embedded embedDtos(List<GroupDto> dtos) {
    return embeddedBuilder()
      .with("groups", dtos)
      .build();
  }
}
