package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import de.otto.edison.hal.paging.NumberedPaging;
import de.otto.edison.hal.paging.PagingRel;
import sonia.scm.PageResult;
import sonia.scm.user.User;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static de.otto.edison.hal.paging.NumberedPaging.zeroBasedNumberedPaging;
import static java.util.stream.Collectors.toList;
import static sonia.scm.api.v2.resources.ResourceLinks.userCollection;

public class UserCollectionToDtoMapper {

  private final UserToUserDtoMapper userToDtoMapper;
  private final UriInfoStore uriInfoStore;

  @Inject
  public UserCollectionToDtoMapper(UserToUserDtoMapper userToDtoMapper, UriInfoStore uriInfoStore) {
    this.userToDtoMapper = userToDtoMapper;
    this.uriInfoStore = uriInfoStore;
  }

  public UserCollectionDto map(int pageNumber, int pageSize, PageResult<User> pageResult) {
    NumberedPaging paging = zeroBasedNumberedPaging(pageNumber, pageSize, pageResult.getOverallCount());
    List<UserDto> dtos = pageResult.getEntities().stream().map(userToDtoMapper::map).collect(toList());

    UserCollectionDto userCollectionDto = new UserCollectionDto(
      createLinks(paging),
      embedDtos(dtos)
    );
    userCollectionDto.setPage(pageNumber);
    return userCollectionDto;
  }

  private Links createLinks(NumberedPaging page) {
    String baseUrl = userCollection(uriInfoStore.get()).self();

    Links.Builder linksBuilder = linkingTo()
      .with(page.links(
        fromTemplate(baseUrl + "{?page,pageSize}"),
        EnumSet.allOf(PagingRel.class)));
    if (UserPermissions.create().isPermitted()) {
      linksBuilder.single(link("create", userCollection(uriInfoStore.get()).create()));
    }
    return linksBuilder.build();
  }

  private Embedded embedDtos(List<UserDto> dtos) {
    return embeddedBuilder()
      .with("users", dtos)
      .build();
  }
}
