package sonia.scm.api.rest.resources;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.user.User;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

import static javax.ws.rs.core.Link.fromUri;

@Mapper
public abstract class User2UserDtoMapper {

  public abstract UserDto userToUserDto(User user, @Context UriInfo uriInfo);

  @AfterMapping
  void removePassword(@MappingTarget UserDto target) {
    target.setPassword(UserResource.DUMMY_PASSWORT);
  }

  @AfterMapping
  void appendLinks(@MappingTarget UserDto target, @Context UriInfo uriInfo) {
    Map<String, Link> links = new LinkedHashMap<>();
    links.put("self", new Link(uriInfo.getBaseUri().resolve(fromUri(UriBuilder.fromResource(UserNewResource.class)
      .path(UserNewResource.class, "get").build(target.getName())).build().getUri())));
    links.put("delete", new Link(uriInfo.getBaseUri().resolve(fromUri(UriBuilder.fromResource(UserNewResource.class)
      .path(UserNewResource.class, "delete").build(target.getName())).build().getUri())));
    links.put("update", new Link(uriInfo.getBaseUri().resolve(fromUri(UriBuilder.fromResource(UserNewResource.class)
      .path(UserNewResource.class, "update").build(target.getName())).build().getUri())));
    links.put("create", new Link(uriInfo.getBaseUri().resolve(fromUri(UriBuilder.fromResource(UserNewResource.class)
      .path(UserNewResource.class, "create").build()).build().getUri())));
    target.setLinks(links);
  }
}
