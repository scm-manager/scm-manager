package sonia.scm.api.rest.resources;

import org.apache.shiro.SecurityUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.security.Role;
import sonia.scm.user.User;

import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

@Mapper
public abstract class User2UserDtoMapper {

  public abstract UserDto userToUserDto(User user, @Context UriInfo uriInfo);

  @AfterMapping
  void removePassword(@MappingTarget UserDto target) {
    target.setPassword(UserResource.DUMMY_PASSWORT);
  }

  @AfterMapping
  void appendLinks(@MappingTarget UserDto target, @Context UriInfo uriInfo) {
    LinkMapBuilder userLinkBuilder = new LinkMapBuilder(uriInfo, UserNewResource.class, UserNewResource.UserSubResource.class);
    LinkMapBuilder collectionLinkBuilder = new LinkMapBuilder(uriInfo, UserNewResource.class, UserNewResource.UsersResource.class);
    userLinkBuilder.add("self").method("getUserSubResource").parameters(target.getName()).method("get").parameters();
    if (SecurityUtils.getSubject().hasRole(Role.ADMIN)) {
      userLinkBuilder.add("delete").method("getUserSubResource").parameters(target.getName()).method("delete").parameters();
      userLinkBuilder.add("update").method("getUserSubResource").parameters(target.getName()).method("update").parameters();
      collectionLinkBuilder.add("create").method("getUsersResource").parameters().method("create").parameters();
    }
    Map<String, Link> join = new HashMap<>();
    join.putAll(userLinkBuilder.getLinkMap());
    join.putAll(collectionLinkBuilder.getLinkMap());
    target.setLinks(join);
  }
}
