package sonia.scm.repository;

import groovy.lang.Singleton;

import javax.inject.Inject;
import java.io.File;

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

  private static final String REPOSITORIES_NATIVE_DIRECTORY = "data";
  private RepositoryDAO repositoryDAO;
  private InitialRepositoryLocationResolver initialRepositoryLocationResolver;

  @Inject
  public RepositoryLocationResolver(RepositoryDAO repositoryDAO, InitialRepositoryLocationResolver initialRepositoryLocationResolver) {
    this.repositoryDAO = repositoryDAO;
    this.initialRepositoryLocationResolver = initialRepositoryLocationResolver;
  }

  public File getRepositoryDirectory(Repository repository){
    if (repositoryDAO instanceof PathBasedRepositoryDAO) {
      PathBasedRepositoryDAO pathBasedRepositoryDAO = (PathBasedRepositoryDAO) repositoryDAO;
      return pathBasedRepositoryDAO.getPath(repository).toFile();
    }
    return initialRepositoryLocationResolver.getDefaultDirectory(repository);
  }

  public File getNativeDirectory(Repository repository)  {
    return new File (getRepositoryDirectory(repository), REPOSITORIES_NATIVE_DIRECTORY);
  }
}
