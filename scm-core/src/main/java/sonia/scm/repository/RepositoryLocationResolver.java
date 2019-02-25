package sonia.scm.repository;

import sonia.scm.SCMContextProvider;

import javax.inject.Inject;
import java.nio.file.Path;

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
public class RepositoryLocationResolver {

  private final SCMContextProvider contextProvider;
  private final RepositoryDAO repositoryDAO;
  private final InitialRepositoryLocationResolver initialRepositoryLocationResolver;

  @Inject
  public RepositoryLocationResolver(SCMContextProvider contextProvider, RepositoryDAO repositoryDAO, InitialRepositoryLocationResolver initialRepositoryLocationResolver) {
    this.contextProvider = contextProvider;
    this.repositoryDAO = repositoryDAO;
    this.initialRepositoryLocationResolver = initialRepositoryLocationResolver;
  }

  /**
   * Returns the path to the repository.
   *
   * @param repositoryId repository id
   *
   * @return path of repository
   */
  public Path getPath(String repositoryId) {
    Path path;

    if (repositoryDAO instanceof PathBasedRepositoryDAO) {
      path = ((PathBasedRepositoryDAO) repositoryDAO).getPath(repositoryId);
    } else {
      path = initialRepositoryLocationResolver.getPath(repositoryId);
    }

    return contextProvider.resolve(path);
  }
}
