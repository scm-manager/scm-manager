package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class UserToUserDtoMapper extends BaseMapper<User, UserDto> {

  @Inject
  private UserManager userManager;

  @Override
  @Mapping(target = "attributes", ignore = true)
  @Mapping(target = "password", ignore = true)
  public abstract UserDto map(User modelObject);

  @Inject
  private ResourceLinks resourceLinks;

  @AfterMapping
  protected void appendLinks(User user, @MappingTarget UserDto target) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.user().self(target.getName()));
    if (UserPermissions.delete(user).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.user().delete(target.getName())));
    }
    if (UserPermissions.modify(user).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.user().update(target.getName())));
      if (userManager.isTypeDefault(user)) {
        linksBuilder.single(link("password", resourceLinks.user().passwordChange(target.getName())));
      }
    }
    if (PermissionPermissions.read().isPermitted()) {
      linksBuilder.single(link("permissions", resourceLinks.userPermissions().permissions(target.getName())));
    }

    appendLinks(new EdisonHalAppender(linksBuilder), user);

    target.add(linksBuilder.build());
  }

}
