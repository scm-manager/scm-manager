package sonia.scm.repository;

import com.google.common.base.Strings;
import org.apache.shiro.SecurityUtils;
import sonia.scm.plugin.Extension;

/**
 * The DefaultNamespaceStrategy returns the predefined namespace of the given repository, if the namespace was not set
 * the username of the currently loggedin user is used.
 *
 * @since 2.0.0
 */
@Extension
public class DefaultNamespaceStrategy implements NamespaceStrategy {

  @Override
  public String createNamespace(Repository repository) {
    String namespace = repository.getNamespace();
    if (Strings.isNullOrEmpty(namespace)) {
      namespace = SecurityUtils.getSubject().getPrincipal().toString();
    }
    return namespace;
  }
}
