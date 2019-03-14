package sonia.scm.repository;

import org.apache.shiro.SecurityUtils;
import sonia.scm.plugin.Extension;

@Extension
public class UsernameNamespaceStrategy implements NamespaceStrategy {

  @Override
  public String createNamespace(Repository repository) {
    return SecurityUtils.getSubject().getPrincipal().toString();
  }
}
