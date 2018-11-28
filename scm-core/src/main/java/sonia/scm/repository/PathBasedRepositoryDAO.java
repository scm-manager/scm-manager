package sonia.scm.repository;

import java.nio.file.Path;

/**
 * A DAO used for Repositories accessible by a path
 *
 * @author Mohamed Karray
 * @since 2.0.0
 */
public interface PathBasedRepositoryDAO extends RepositoryDAO {

  /**
   * Get the current path of the repository. This works for existing repositories only, not for repositories that should be created.
   */
  Path getPath(Repository repository) ;
}
