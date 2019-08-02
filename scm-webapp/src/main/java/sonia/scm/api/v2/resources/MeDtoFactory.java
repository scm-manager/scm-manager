package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import sonia.scm.group.GroupCollector;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

public class MeDtoFactory extends HalAppenderMapper {

  private final ResourceLinks resourceLinks;
  private final UserManager userManager;
  private final GroupCollector groupCollector;

  @Inject
  public MeDtoFactory(ResourceLinks resourceLinks, UserManager userManager, GroupCollector groupCollector) {
    this.resourceLinks = resourceLinks;
    this.userManager = userManager;
    this.groupCollector = groupCollector;
  }

  public MeDto create() {
    PrincipalCollection principals = getPrincipalCollection();
    User user = principals.oneByType(User.class);

    MeDto dto = createDto(user);
    mapUserProperties(user, dto);
    mapGroups(user, dto);
    return dto;
  }

  private void mapGroups(User user, MeDto dto) {
    dto.setGroups(groupCollector.collect(user.getName()));
  }

  private void mapUserProperties(User user, MeDto dto) {
    dto.setName(user.getName());
    dto.setDisplayName(user.getDisplayName());
    dto.setMail(user.getMail());
  }

  private PrincipalCollection getPrincipalCollection() {
    Subject subject = SecurityUtils.getSubject();
    return subject.getPrincipals();
  }


  private MeDto createDto(User user) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.me().self());
    if (UserPermissions.delete(user).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.me().delete(user.getName())));
    }
    if (UserPermissions.modify(user).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.me().update(user.getName())));
    }
    if (userManager.isTypeDefault(user) && UserPermissions.changePassword(user).isPermitted()) {
      linksBuilder.single(link("password", resourceLinks.me().passwordChange()));
    }

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), new Me(), user);

    return new MeDto(linksBuilder.build(), embeddedBuilder.build());
  }

}
