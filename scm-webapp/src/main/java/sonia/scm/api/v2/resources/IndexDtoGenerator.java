package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.apache.shiro.SecurityUtils;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.group.GroupPermissions;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;

public class IndexDtoGenerator {

  private final ResourceLinks resourceLinks;

  @Inject
  public IndexDtoGenerator(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  public IndexDto generate() {
    Links.Builder builder = Links.linkingTo();
    if (SecurityUtils.getSubject().isAuthenticated()) {
      builder.single(
        Link.link("me", resourceLinks.me().self()),
        Link.link("logout", resourceLinks.authentication().logout())
      );
      if (UserPermissions.list().isPermitted()) {
        builder.single(Link.link("users", resourceLinks.userCollection().self()));
      }
      if (GroupPermissions.list().isPermitted()) {
        builder.single(Link.link("groups", resourceLinks.groupCollection().self()));
      }
      if (ConfigurationPermissions.list().isPermitted()) {
        builder.single(Link.link("config", resourceLinks.config().self()));
      }
      builder.single(Link.link("repositories", resourceLinks.repositoryCollection().self()));
    } else {
      builder.single(
        Link.link("formLogin", resourceLinks.authentication().formLogin()),
        Link.link("jsonLogin", resourceLinks.authentication().jsonLogin())
      );
    }

    return new IndexDto(builder.build());
  }
}
