package sonia.scm.api.rest.resources;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.user.User;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.LinkedHashMap;
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
    Map<String, Link> links = new LinkedHashMap<>();
    links.put("self", createLink("get", uriInfo, target.getName()));
    links.put("delete", createLink("delete", uriInfo, target.getName()));
    links.put("update", createLink("update", uriInfo, target.getName()));
    links.put("create", createLink("create", uriInfo));
    target.setLinks(links);
  }

  private Link createLink(String methodName, UriInfo uriInfo, String... parameters) {
    URI baseUri = uriInfo.getBaseUri();
    URI relativeUri = createRelativeUri(methodName, parameters);
    URI absoluteUri = baseUri.resolve(relativeUri);
    return new Link(absoluteUri);
  }

  private URI createRelativeUri(String methodName, Object[] parameters) {
    return userUriBuilder().path(UserNewResource.class, methodName).build(parameters);
  }

  private UriBuilder userUriBuilder() {
    return UriBuilder.fromResource(UserNewResource.class);
  }
}
