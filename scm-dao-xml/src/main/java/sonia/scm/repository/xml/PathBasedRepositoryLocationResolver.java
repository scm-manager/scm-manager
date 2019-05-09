package sonia.scm.repository.xml;

import sonia.scm.SCMContextProvider;
import sonia.scm.repository.BasicRepositoryLocationResolver;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.PathBasedRepositoryDAO;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryLocationResolver;

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
public class PathBasedRepositoryLocationResolver extends BasicRepositoryLocationResolver<Path> {

  private final SCMContextProvider contextProvider;
  private final InitialRepositoryLocationResolver initialRepositoryLocationResolver;
  private final RepositoryDAO repositoryDAO;

  @Inject
  public PathBasedRepositoryLocationResolver(SCMContextProvider contextProvider, RepositoryDAO repositoryDAO, InitialRepositoryLocationResolver initialRepositoryLocationResolver) {
    super(Path.class);
    this.contextProvider = contextProvider;
    this.initialRepositoryLocationResolver = initialRepositoryLocationResolver;
    this.repositoryDAO = repositoryDAO;
  }

  @Override
  protected <T> RepositoryLocationResolverInstance<T> create(Class<T> type) {
    return repositoryId -> {
      Path path;

      if (repositoryDAO instanceof PathBasedRepositoryDAO) {
        path = ((PathBasedRepositoryDAO) repositoryDAO).getPath(repositoryId);
      } else {
        path = initialRepositoryLocationResolver.getPath(repositoryId);
      }

      return (T) contextProvider.resolve(path);
    };
  }
}
