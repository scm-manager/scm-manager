package sonia.scm.repository;

import org.apache.shiro.SecurityUtils;
import sonia.scm.plugin.Extension;

/**
 * The DefaultNamespaceStrategy returns the username of the currently logged in user as namespace.
 * @since 2.0.0
 */
@Extension
public class DefaultNamespaceStrategy implements NamespaceStrategy {

  @Override
  public String getNamespace() {
    return SecurityUtils.getSubject().getPrincipal().toString();
  }
}
