package sonia.scm.repository.xml;

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.BasicRepositoryLocationResolver;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.Repository;
import sonia.scm.store.StoreConstants;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

  private static final String STORE_NAME = "repositories";

  private final SCMContextProvider contextProvider;
  private final InitialRepositoryLocationResolver initialRepositoryLocationResolver;

  private final SCMContextProvider context;

  private final PathDatabase pathDatabase;
  private final Map<String, Path> pathById;

  private final Clock clock;

  private Long creationTime;
  private Long lastModified;

  @Inject
  public PathBasedRepositoryLocationResolver(SCMContextProvider contextProvider, InitialRepositoryLocationResolver initialRepositoryLocationResolver, SCMContextProvider context) {
    this(contextProvider, initialRepositoryLocationResolver, context, Clock.systemUTC());
  }

  public PathBasedRepositoryLocationResolver(SCMContextProvider contextProvider, InitialRepositoryLocationResolver initialRepositoryLocationResolver, SCMContextProvider context, Clock clock) {
    super(Path.class);
    this.contextProvider = contextProvider;
    this.initialRepositoryLocationResolver = initialRepositoryLocationResolver;
    this.context = context;
    this.pathById = new ConcurrentHashMap<>();

    this.clock = clock;

    this.creationTime = clock.millis();
    pathDatabase = new PathDatabase(resolveStorePath());

    read();
  }

  @Override
  protected <T> RepositoryLocationResolverInstance<T> create(Class<T> type) {
    return repositoryId -> {
      Path path;
      if (pathById.containsKey(repositoryId)) {
        path = pathById.get(repositoryId);
      } else {
        path = create(repositoryId);
      }
      return (T) contextProvider.resolve(path);
    };
  }

  Path create(String repositoryId) {
    Path path = initialRepositoryLocationResolver.getPath(repositoryId);
    pathById.put(repositoryId, path);
    writePathDatabase();
    return path;
  }

  Path remove(String repositoryId) {
    Path removedPath = pathById.remove(repositoryId);
    writePathDatabase();
    return removedPath;
  }

  private void writePathDatabase() {
    lastModified = clock.millis();
    pathDatabase.write(creationTime, lastModified, pathById);
  }

  private void read() {
    Path storePath = resolveStorePath();

    // Files.exists is slow on java 8
    if (storePath.toFile().exists()) {
      pathDatabase.read(this::onLoadDates, this::onLoadRepository);
    }
  }

  private void onLoadDates(Long creationTime, Long lastModified) {
    this.creationTime = creationTime;
    this.lastModified = lastModified;
  }

  public Long getCreationTime() {
    return creationTime;
  }

  public Long getLastModified() {
    return lastModified;
  }


  private void onLoadRepository(String id, Path repositoryPath) {
//    Path metadataPath = resolveMetadataPath(context.resolve(repositoryPath));
//
//    Repository repository = metadataStore.read(metadataPath);
//
//    byId.put(id, repository);
//    byNamespaceAndName.put(repository.getNamespaceAndName(), repository);
    pathById.put(id, repositoryPath);
  }

  @VisibleForTesting
  Path resolveMetadataPath(Path repositoryPath) {
    return repositoryPath.resolve(StoreConstants.REPOSITORY_METADATA.concat(StoreConstants.FILE_EXTENSION));
  }

  @VisibleForTesting
  Path resolveStorePath() {
    return context.getBaseDirectory()
      .toPath()
      .resolve(StoreConstants.CONFIG_DIRECTORY_NAME)
      .resolve(STORE_NAME.concat(StoreConstants.FILE_EXTENSION));
  }
}
