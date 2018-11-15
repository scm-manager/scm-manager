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
   * get the current path of the repository
   *
   * @param repository
   * @return the current path of the repository
   */
  Path getPath(Repository repository) throws RepositoryPathNotFoundException;
}
