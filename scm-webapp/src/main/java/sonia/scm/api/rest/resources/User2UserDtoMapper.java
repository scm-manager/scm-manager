package sonia.scm.api.rest.resources;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.user.User;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
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
    LinkMapBuilder builder = new LinkMapBuilder(uriInfo);
    builder.add("self", "get", target.getName());
    builder.add("delete", "delete", target.getName());
    builder.add("update", "update", target.getName());
    builder.add("create", "create");
    target.setLinks(builder.getLinkMap());
  }

  private static class LinkMapBuilder {
    private final UriInfo uriInfo;
    private final Map<String, Link> links = new LinkedHashMap<>();

    private LinkMapBuilder(UriInfo uriInfo) {
      this.uriInfo = uriInfo;
    }

    void add(String linkName, String methodName, String... parameters) {
      links.put(linkName, createLink(methodName, parameters));
    }

    Map<String, Link> getLinkMap() {
      return Collections.unmodifiableMap(links);
    }

    private Link createLink(String methodName, String... parameters) {
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
}
