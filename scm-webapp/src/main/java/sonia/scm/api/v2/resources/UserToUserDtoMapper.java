package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.api.rest.resources.UserResource;
import sonia.scm.user.User;
import sonia.scm.user.UserPermissions;

import javax.ws.rs.core.UriInfo;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static sonia.scm.api.v2.resources.ResourceLinks.user;

@Mapper
public abstract class UserToUserDtoMapper extends BaseMapper {

  public abstract UserDto map(User user, @Context UriInfo uriInfo);

  @AfterMapping
  void removePassword(@MappingTarget UserDto target) {
    target.setPassword(UserResource.DUMMY_PASSWORT);
  }

  @AfterMapping
  void appendLinks(User user, @MappingTarget UserDto target, @Context UriInfo uriInfo) {
    Links.Builder linksBuilder = linkingTo().self(user(uriInfo).self(target.getName()));
    if (UserPermissions.delete(user).isPermitted()) {
      linksBuilder.single(link("delete", user(uriInfo).delete(target.getName())));
    }
    if (UserPermissions.modify(user).isPermitted()) {
      linksBuilder.single(link("update", user(uriInfo).update(target.getName())));
    }
    target.add(
      linksBuilder.build());
  }
}
