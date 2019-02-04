package sonia.scm.api.v2.resources;

import com.google.common.collect.ImmutableList;
import de.otto.edison.hal.Links;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import sonia.scm.group.GroupNames;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;
import java.util.Collections;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

public class MeDtoFactory extends HalAppenderMapper {

  private final ResourceLinks resourceLinks;
  private final UserManager userManager;

  @Inject
  public MeDtoFactory(ResourceLinks resourceLinks, UserManager userManager) {
    this.resourceLinks = resourceLinks;
    this.userManager = userManager;
  }

  public MeDto create() {
    PrincipalCollection principals = getPrincipalCollection();

    MeDto dto = new MeDto();

    User user = principals.oneByType(User.class);

    mapUserProperties(user, dto);
    mapGroups(principals, dto);

    appendLinks(user, dto);
    return dto;
  }

  private void mapGroups(PrincipalCollection principals, MeDto dto) {
    Iterable<String> groups = principals.oneByType(GroupNames.class);
    if (groups == null) {
      groups = Collections.emptySet();
    }
    dto.setGroups(ImmutableList.copyOf(groups));
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


  private void appendLinks(User user, MeDto target) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.me().self());
    if (UserPermissions.delete(user).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.me().delete(target.getName())));
    }
    if (UserPermissions.modify(user).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.me().update(target.getName())));
    }
    if (userManager.isTypeDefault(user) && UserPermissions.changePassword(user).isPermitted()) {
      linksBuilder.single(link("password", resourceLinks.me().passwordChange()));
    }

    appendLinks(new EdisonHalAppender(linksBuilder), new Me(), user);

    target.add(linksBuilder.build());
  }

}
