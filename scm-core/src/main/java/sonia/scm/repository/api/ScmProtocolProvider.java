package sonia.scm.repository.api;

import sonia.scm.plugin.ExtensionPoint;
import sonia.scm.repository.Repository;

/**
 * Provider for scm native protocols.
 *
 * @param <T> type of protocol
 *
 * @since 2.0.0
 */
@ExtensionPoint(multi = true)
public interface ScmProtocolProvider<T extends ScmProtocol> {

  /**
   * Returns type of repository (e.g.: git, svn, hg, etc.)
   *
   * @return name of type
   */
  String getType();

  /**
   * Returns protocol for the given repository.
   *
   * @param repository repository
   *
   * @return protocol for repository
   */
  T get(Repository repository);
}
