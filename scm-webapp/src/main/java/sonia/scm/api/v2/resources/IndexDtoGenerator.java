package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.apache.shiro.SecurityUtils;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.group.GroupPermissions;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;

public class IndexDtoGenerator {

  private final ResourceLinks resourceLinks;

  @Inject
  public IndexDtoGenerator(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  public IndexDto generate() {
    Links.Builder builder = Links.linkingTo();
    builder.self(resourceLinks.index().self());
    builder.single(link("uiPlugins", resourceLinks.uiPluginCollection().self()));
    if (SecurityUtils.getSubject().isAuthenticated()) {
      builder.single(
        link("me", resourceLinks.me().self()),
        link("logout", resourceLinks.authentication().logout())
      );
      if (UserPermissions.list().isPermitted()) {
        builder.single(link("users", resourceLinks.userCollection().self()));
      }
      if (GroupPermissions.list().isPermitted()) {
        builder.single(link("groups", resourceLinks.groupCollection().self()));
      }
      if (ConfigurationPermissions.list().isPermitted()) {
        builder.single(link("config", resourceLinks.config().self()));
      }
      builder.single(link("repositories", resourceLinks.repositoryCollection().self()));
    } else {
      builder.single(
        link("formLogin", resourceLinks.authentication().formLogin()),
        link("jsonLogin", resourceLinks.authentication().jsonLogin())
      );
    }

    return new IndexDto(builder.build());
  }
}
