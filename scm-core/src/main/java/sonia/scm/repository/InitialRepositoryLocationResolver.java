package sonia.scm.repository;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A Location Resolver for File based Repository Storage.
 * <p>
 * <b>WARNING:</b> The Locations provided with this class may not be used from the plugins to store any plugin specific files.
 * <p>
 * Please use the {@link sonia.scm.store.DataStoreFactory } and the {@link sonia.scm.store.DataStore} classes to store data<br>
 * Please use the {@link sonia.scm.store.BlobStoreFactory } and the {@link sonia.scm.store.BlobStore} classes to store binary files<br>
 * Please use the {@link sonia.scm.store.ConfigurationStoreFactory} and the {@link sonia.scm.store.ConfigurationStore} classes  to store configurations
 *
 * @author Mohamed Karray
 * @since 2.0.0
 */
public class InitialRepositoryLocationResolver {

  private static final String DEFAULT_REPOSITORY_PATH = "repositories";

  /**
   * Returns the initial path to repository.
   *
   * @param repositoryId id of the repository
   *
   * @return initial path of repository
   */
  public Path getPath(String repositoryId) {
    return Paths.get(DEFAULT_REPOSITORY_PATH, repositoryId);
  }

}
