package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import de.otto.edison.hal.paging.NumberedPaging;
import de.otto.edison.hal.paging.PagingRel;
import sonia.scm.PageResult;
import sonia.scm.user.User;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static de.otto.edison.hal.paging.NumberedPaging.zeroBasedNumberedPaging;

public class UserCollection2DtoMapper {

  private final User2UserDtoMapper userToDtoMapper;

  @Inject
  public UserCollection2DtoMapper(User2UserDtoMapper userToDtoMapper) {
    this.userToDtoMapper = userToDtoMapper;
  }

  public UserCollectionDto map(UriInfo uriInfo, int pageNumber, int pageSize, PageResult<User> pageResult) {
    NumberedPaging paging = zeroBasedNumberedPaging(pageNumber, pageSize, pageResult.hasMore());
    List<UserDto> dtos = pageResult.getEntities().stream().map(user -> userToDtoMapper.map(user, uriInfo)).collect(Collectors.toList());

    UserCollectionDto userCollectionDto = new UserCollectionDto(
      createLinks(uriInfo, paging),
      embedDtos(dtos)
    );
    userCollectionDto.setPage(pageNumber);
    return userCollectionDto;
  }

  private static Links createLinks(UriInfo uriInfo, NumberedPaging page) {
    LinkBuilder collectionLinkBuilder = new LinkBuilder(uriInfo, UserV2Resource.class, UserCollectionResource.class);
    String baseUrl = collectionLinkBuilder.method("getUserCollectionResource").parameters().method("create").parameters().href();

    Links.Builder linksBuilder = linkingTo()
      .with(page.links(
        fromTemplate(baseUrl + "{?page,pageSize}"),
        EnumSet.allOf(PagingRel.class)));
    if (UserPermissions.create().isPermitted()) {
      linksBuilder
        .single(link("create", collectionLinkBuilder. method("getUserCollectionResource").parameters().method("create").parameters().href()));
    }
    return linksBuilder.build();
  }

  private Embedded embedDtos(List<UserDto> dtos) {
    return embeddedBuilder()
      .with("users", dtos)
      .build();
  }
}
