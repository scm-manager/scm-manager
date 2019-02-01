package sonia.scm.api.v2.resources;

import com.google.common.collect.Lists;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.apache.shiro.SecurityUtils;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.group.GroupPermissions;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;
import java.util.List;

import static de.otto.edison.hal.Link.link;

public class IndexDtoGenerator extends LinkAppenderMapper {

  private final ResourceLinks resourceLinks;
  private final SCMContextProvider scmContextProvider;

  @Inject
  public IndexDtoGenerator(ResourceLinks resourceLinks, SCMContextProvider scmContextProvider) {
    this.resourceLinks = resourceLinks;
    this.scmContextProvider = scmContextProvider;
  }

  public IndexDto generate() {
    Links.Builder builder = Links.linkingTo();
    List<Link> autoCompleteLinks = Lists.newArrayList();
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
      if (UserPermissions.autocomplete().isPermitted()) {
        autoCompleteLinks.add(Link.linkBuilder("autocomplete", resourceLinks.autoComplete().users()).withName("users").build());
      }
      if (GroupPermissions.autocomplete().isPermitted()) {
        autoCompleteLinks.add(Link.linkBuilder("autocomplete", resourceLinks.autoComplete().groups()).withName("groups").build());
      }
      builder.array(autoCompleteLinks);
      if (GroupPermissions.list().isPermitted()) {
        builder.single(link("groups", resourceLinks.groupCollection().self()));
      }
      if (ConfigurationPermissions.list().isPermitted()) {
        builder.single(link("config", resourceLinks.config().self()));
      }
      builder.single(link("repositories", resourceLinks.repositoryCollection().self()));
      if (PermissionPermissions.list().isPermitted()) {
        builder.single(link("permissions", resourceLinks.permissions().self()));
      }
      builder.single(link("availableRepositoryPermissions", resourceLinks.availableRepositoryPermissions().self()));
    } else {
      builder.single(link("login", resourceLinks.authentication().jsonLogin()));
    }

    appendLinks(new EdisonLinkAppender(builder), new Index());

    return new IndexDto(scmContextProvider.getVersion(), builder.build());
  }
}
