package sonia.scm.repository;

import groovy.lang.Singleton;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static sonia.scm.repository.InitialRepositoryLocationResolver.REPOSITORIES_NATIVE_DIRECTORY;

/**
 *
 * A Location Resolver for File based Repository Storage.
 *
 * WARNING: The Locations provided with this class may not be used from the plugins to store any plugin specific files.
 *
 * Please use the {@link sonia.scm.store.DataStoreFactory } and the {@link sonia.scm.store.DataStore} classes to store data
 * Please use the {@link sonia.scm.store.BlobStoreFactory } and the {@link sonia.scm.store.BlobStore} classes to store binary files
 * Please use the {@link sonia.scm.store.ConfigurationStoreFactory} and the {@link sonia.scm.store.ConfigurationStore} classes  to store configurations
 *
 * @author Mohamed Karray
 * @since 2.0.0
 */
@Singleton
public class RepositoryLocationResolver {

  private static final String REPOSITORIES_STORES_DIRECTORY = "stores";
  private static final String REPOSITORIES_CONFIG_DIRECTORY = "config";
  private static final String GLOBAL_STORE_BASE_DIRECTORY = "var";

  private RepositoryDAO repositoryDAO;
  private InitialRepositoryLocationResolver initialRepositoryLocationResolver;

  @Inject
  public RepositoryLocationResolver(RepositoryDAO repositoryDAO, InitialRepositoryLocationResolver initialRepositoryLocationResolver) {
    this.repositoryDAO = repositoryDAO;
    this.initialRepositoryLocationResolver = initialRepositoryLocationResolver;
  }

  /**
   * Get the current repository directory from the dao or create the initial directory if the repository does not exists
   * @param repository
   * @return the current repository directory from the dao or the initial directory if the repository does not exists
   * @throws IOException
   */
  public File getRepositoryDirectory(Repository repository) throws IOException {
    if (repositoryDAO instanceof PathBasedRepositoryDAO) {
      PathBasedRepositoryDAO pathBasedRepositoryDAO = (PathBasedRepositoryDAO) repositoryDAO;
      try {
        return pathBasedRepositoryDAO.getPath(repository).toFile();
      } catch (RepositoryPathNotFoundException e) {
        return createInitialDirectory(repository);
      }
    }
    return createInitialDirectory(repository);
  }

  public File getInitialBaseDirectory() {
    return initialRepositoryLocationResolver.getBaseDirectory();
  }

  public File createInitialDirectory(Repository repository) throws IOException {
    return initialRepositoryLocationResolver.createDirectory(repository);
  }

  public File getInitialDirectory(Repository repository) {
    return initialRepositoryLocationResolver.getDirectory(repository);
  }

  public File getNativeDirectory(Repository repository) throws IOException {
    return new File (getRepositoryDirectory(repository), REPOSITORIES_NATIVE_DIRECTORY);
  }

  public File getInitialNativeDirectory(Repository repository) {
    return new File (getInitialDirectory(repository), REPOSITORIES_NATIVE_DIRECTORY);
  }

  /**
   * Get the store directory of a specific repository
   * @param repository
   * @return the store directory of a specific repository
   */
  public File getStoresDirectory(Repository repository) throws IOException{
    return new File (getRepositoryDirectory(repository), REPOSITORIES_STORES_DIRECTORY);
  }

  public File getConfigDirectory(Repository repository) throws IOException {
    return new File (getRepositoryDirectory(repository), REPOSITORIES_CONFIG_DIRECTORY);
  }

  public File getGlobalStoreDirectory() {
    return new File(initialRepositoryLocationResolver.getContextBaseDirectory(),
      GLOBAL_STORE_BASE_DIRECTORY.concat(File.separator));
  }
}
