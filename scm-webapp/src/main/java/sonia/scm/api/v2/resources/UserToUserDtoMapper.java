package sonia.scm.api.v2.resources;

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.api.rest.resources.UserResource;
import sonia.scm.user.User;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class UserToUserDtoMapper extends BaseMapper<User, UserDto> {

  @Inject
  private ResourceLinks resourceLinks;

  @VisibleForTesting
  void setResourceLinks(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  @AfterMapping
  void removePassword(@MappingTarget UserDto target) {
    target.setPassword(UserResource.DUMMY_PASSWORT);
  }

  @AfterMapping
  void appendLinks(User user, @MappingTarget UserDto target) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.user().self(target.getName()));
    if (UserPermissions.delete(user).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.user().delete(target.getName())));
    }
    if (UserPermissions.modify(user).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.user().update(target.getName())));
    }
    target.add(linksBuilder.build());
  }

}
