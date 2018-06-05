package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.apache.shiro.SecurityUtils;
import org.mapstruct.*;
import sonia.scm.api.rest.resources.UserResource;
import sonia.scm.security.Role;
import sonia.scm.user.User;

import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.Optional;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class User2UserDtoMapper {

  public abstract UserDto userToUserDto(User user, @Context UriInfo uriInfo);

  @AfterMapping
  void removePassword(@MappingTarget UserDto target) {
    target.setPassword(UserResource.DUMMY_PASSWORT);
  }

  @AfterMapping
  void appendLinks(@MappingTarget UserDto target, @Context UriInfo uriInfo) {
    LinkBuilder userLinkBuilder = new LinkBuilder(uriInfo, UserV2Resource.class, UserSubResource.class);
    LinkBuilder collectionLinkBuilder = new LinkBuilder(uriInfo, UserV2Resource.class, UserCollectionResource.class);

    Links.Builder linksBuilder = linkingTo()
      .self(userLinkBuilder.method("getUserSubResource").parameters(target.getName()).method("get").parameters().href());
    if (SecurityUtils.getSubject().hasRole(Role.ADMIN)) {
      linksBuilder
        .single(link("delete", userLinkBuilder.method("getUserSubResource").parameters(target.getName()).method("delete").parameters().href()))
        .single(link("update", userLinkBuilder.method("getUserSubResource").parameters(target.getName()).method("update").parameters().href()))
        .single(link("create", collectionLinkBuilder. method("getUserCollectionResource").parameters().method("create").parameters().href()));
    }
    target.add(
      linksBuilder.build());
  }

  @Mapping(target = "creationDate")
  Instant mapTime(Long epochMilli) {
    // TODO assert parameter not null
    return Instant.ofEpochMilli(epochMilli);
  }

  @Mapping(target = "lastModified")
  Optional<Instant> mapOptionalTime(Long epochMilli) {
    return Optional
      .ofNullable(epochMilli)
      .map(Instant::ofEpochMilli);
  }
}
