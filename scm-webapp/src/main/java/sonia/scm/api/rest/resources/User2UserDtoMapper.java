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
    LinkBuilder userLinkBuilder = new LinkBuilder(uriInfo, UserNewResource.class, UserSubResource.class);
    LinkBuilder collectionLinkBuilder = new LinkBuilder(uriInfo, UserNewResource.class, UserCollectionResource.class);
    Map<String, Link> links = new HashMap<>();
    links.put("self", userLinkBuilder.method("getUserSubResource").parameters(target.getName()).method("get").parameters().create());
    if (SecurityUtils.getSubject().hasRole(Role.ADMIN)) {
      links.put("delete", userLinkBuilder.method("getUserSubResource").parameters(target.getName()).method("delete").parameters().create());
      links.put("update", userLinkBuilder.method("getUserSubResource").parameters(target.getName()).method("update").parameters().create());
      links.put("create", collectionLinkBuilder.method("getUserCollectionResource").parameters().method("create").parameters().create());
    }
    target.setLinks(links);
  }
}
