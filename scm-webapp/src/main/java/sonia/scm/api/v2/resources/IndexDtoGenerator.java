package sonia.scm.api.v2.resources;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.apache.shiro.SecurityUtils;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.GroupPermissions;
import sonia.scm.plugin.PluginPermissions;
import sonia.scm.repository.RepositoryRolePermissions;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;
import java.util.List;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;

public class IndexDtoGenerator extends HalAppenderMapper {

  private final ResourceLinks resourceLinks;
  private final SCMContextProvider scmContextProvider;
  private final ScmConfiguration configuration;

  @Inject
  public IndexDtoGenerator(ResourceLinks resourceLinks, SCMContextProvider scmContextProvider, ScmConfiguration configuration) {
    this.resourceLinks = resourceLinks;
    this.scmContextProvider = scmContextProvider;
    this.configuration = configuration;
  }

  public IndexDto generate() {
    Links.Builder builder = Links.linkingTo();
    List<Link> autoCompleteLinks = Lists.newArrayList();
    builder.self(resourceLinks.index().self());
    builder.single(link("uiPlugins", resourceLinks.uiPluginCollection().self()));

    String loginInfoUrl = configuration.getLoginInfoUrl();
    if (!Strings.isNullOrEmpty(loginInfoUrl)) {
      builder.single(link("loginInfo", loginInfoUrl));
    }

    if (SecurityUtils.getSubject().isAuthenticated()) {
      builder.single(
        link("me", resourceLinks.me().self()),
        link("logout", resourceLinks.authentication().logout())
      );
      if (PluginPermissions.read().isPermitted()) {
        builder.single(link("installedPlugins", resourceLinks.installedPluginCollection().self()));
        builder.single(link("availablePlugins", resourceLinks.availablePluginCollection().self()));
      }
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
      builder.single(link("repositoryVerbs", resourceLinks.repositoryVerbs().self()));

      builder.single(link("repositoryTypes", resourceLinks.repositoryTypeCollection().self()));
      builder.single(link("namespaceStrategies", resourceLinks.namespaceStrategies().self()));
      builder.single(link("repositoryRoles", resourceLinks.repositoryRoleCollection().self()));
    } else {
      builder.single(link("login", resourceLinks.authentication().jsonLogin()));
    }

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(builder, embeddedBuilder), new Index());

    return new IndexDto(builder.build(), embeddedBuilder.build(), scmContextProvider.getVersion());
  }
}
