package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class MeToUserDtoMapper extends UserToUserDtoMapper{

  @Inject
  private UserManager userManager;

  @Inject
  private ResourceLinks resourceLinks;


  @AfterMapping
  void appendLinks(User user, @MappingTarget UserDto target) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.me().self());
    if (UserPermissions.delete(user).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.me().delete(target.getName())));
    }
    if (UserPermissions.modify(user).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.me().update(target.getName())));
    }
    if (userManager.isTypeDefault(user)) {
      linksBuilder.single(link("password", resourceLinks.me().passwordChange()));
    }
    target.add(linksBuilder.build());
  }

}
