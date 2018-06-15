package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.api.rest.resources.UserResource;
import sonia.scm.user.User;
import sonia.scm.user.UserPermissions;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static sonia.scm.api.v2.resources.ResourceLinks.user;

@Mapper
public abstract class UserToUserDtoMapper extends BaseMapper {

  @Inject
  private UriInfoStore uriInfoStore;

  public abstract UserDto map(User user);

  @AfterMapping
  void removePassword(@MappingTarget UserDto target) {
    target.setPassword(UserResource.DUMMY_PASSWORT);
  }

  @AfterMapping
  void appendLinks(User user, @MappingTarget UserDto target) {
    Links.Builder linksBuilder = linkingTo().self(user(uriInfoStore.get()).self(target.getName()));
    if (UserPermissions.delete(user).isPermitted()) {
      linksBuilder.single(link("delete", user(uriInfoStore.get()).delete(target.getName())));
    }
    if (UserPermissions.modify(user).isPermitted()) {
      linksBuilder.single(link("update", user(uriInfoStore.get()).update(target.getName())));
    }
    target.add(
      linksBuilder.build());
  }
}
