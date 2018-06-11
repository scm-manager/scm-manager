package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Qualifier;
import sonia.scm.PageResult;
import sonia.scm.group.Group;
import sonia.scm.group.GroupPermissions;
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;

import javax.ws.rs.core.UriInfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.Arrays.asList;

@Mapper
public abstract class Group2GroupDtoMapper {

  public abstract GroupDto groupToGroupDto(Group group, @Context UriInfo uriInfo);

  @Inject
  private User2UserDtoMapper user2UserDtoMapper;

  @AfterMapping
  void appendLinks(Group group, @MappingTarget GroupDto target, @Context UriInfo uriInfo) {
    LinkBuilder groupLinkBuilder = new LinkBuilder(uriInfo, GroupV2Resource.class, GroupSubResource.class);

    Links.Builder linksBuilder = linkingTo()
      .self(groupLinkBuilder.method("getGroupSubResource").parameters(target.getName()).method("get").parameters().href());
    if (GroupPermissions.delete(group).isPermitted()) {
      linksBuilder
        .single(link("delete", groupLinkBuilder.method("getGroupSubResource").parameters(target.getName()).method("delete").parameters().href()));
    }
    if (GroupPermissions.modify(group).isPermitted()) {
      linksBuilder
        .single(link("update", groupLinkBuilder.method("getGroupSubResource").parameters(target.getName()).method("update").parameters().href()));
    }
    target.add(linksBuilder.build());
  }

  @AfterMapping
  void appendUserLinks(Group group, @MappingTarget GroupDto target, @Context UriInfo uriInfo) {
    Links.Builder linksBuilder = linkingTo();
    LinkBuilder userLinkBuilder = new LinkBuilder(uriInfo, UserV2Resource.class, UserSubResource.class);
    group.getMembers().forEach(name -> linksBuilder.array(Link.link("users", userLinkBuilder.method("getUserSubResource").parameters(name).method("get").parameters().href())));

    target.add(linksBuilder.build());
  }

  @AfterMapping
  void embedUsers(Group group, @MappingTarget GroupDto target, @Context UriInfo uriInfo) {
    List<UserDto> users = group.getMembers().stream().map(this::createUser).map(u -> user2UserDtoMapper.userToUserDto(u, uriInfo)).collect(Collectors.toList());
    target.withEmbedded("users", users);
  }

  private User createUser(String ich) {
    User user = new User(ich);
    user.setCreationDate(0L);
    return user;
  }

  @Mapping(target = "creationDate")
  Instant mapTime(Long epochMilli) {
    AssertUtil.assertIsNotNull(epochMilli);
    return Instant.ofEpochMilli(epochMilli);
  }

  @Mapping(target = "lastModified")
  Optional<Instant> mapOptionalTime(Long epochMilli) {
    return Optional
      .ofNullable(epochMilli)
      .map(Instant::ofEpochMilli);
  }
}
