package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.api.rest.resources.UserResource;
import sonia.scm.user.User;
import sonia.scm.user.UserPermissions;
import sonia.scm.util.AssertUtil;

import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.Optional;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class User2UserDtoMapper {

  public abstract UserDto map(User user, @Context UriInfo uriInfo);

  @AfterMapping
  void removePassword(@MappingTarget UserDto target) {
    target.setPassword(UserResource.DUMMY_PASSWORT);
  }

  @AfterMapping
  void appendLinks(User user, @MappingTarget UserDto target, @Context UriInfo uriInfo) {
    LinkBuilder userLinkBuilder = new LinkBuilder(uriInfo, UserV2Resource.class, UserSubResource.class);

    Links.Builder linksBuilder = linkingTo()
      .self(userLinkBuilder.method("getUserSubResource").parameters(target.getName()).method("get").parameters().href());
    if (UserPermissions.delete(user).isPermitted()) {
      linksBuilder
        .single(link("delete", userLinkBuilder.method("getUserSubResource").parameters(target.getName()).method("delete").parameters().href()));
    }
    if (UserPermissions.modify(user).isPermitted()) {
      linksBuilder
        .single(link("update", userLinkBuilder.method("getUserSubResource").parameters(target.getName()).method("update").parameters().href()));
    }
    target.add(
      linksBuilder.build());
  }

  Instant mapTime(Long epochMilli) {
    AssertUtil.assertIsNotNull(epochMilli);
    return Instant.ofEpochMilli(epochMilli);
  }

  Optional<Instant> mapOptionalTime(Long epochMilli) {
    return Optional
      .ofNullable(epochMilli)
      .map(Instant::ofEpochMilli);
  }
}
