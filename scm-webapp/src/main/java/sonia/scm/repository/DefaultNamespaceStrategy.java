package sonia.scm.repository;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.plugin.Extension;
import sonia.scm.user.User;


@Extension
public class DefaultNamespaceStrategy implements NamespaceStrategy {

  @Override
  public String getNamespace() {
    Subject subject = SecurityUtils.getSubject();
    String displayName = subject.getPrincipals().oneByType(User.class).getName();
    return displayName;
  }
}
