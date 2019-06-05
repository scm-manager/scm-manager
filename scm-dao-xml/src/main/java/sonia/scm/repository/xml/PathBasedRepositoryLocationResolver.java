package sonia.scm.repository.xml;

import sonia.scm.SCMContextProvider;
import sonia.scm.repository.BasicRepositoryLocationResolver;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.store.StoreConstants;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

/**
 * A Location Resolver for File based Repository Storage.
 * <p>
 * <b>WARNING:</b> The Locations provided with this class may not be used from the plugins to store any plugin specific files.
 * <p>
 * Please use the {@link sonia.scm.store.DataStoreFactory } and the {@link sonia.scm.store.DataStore} classes to store data<br>
 * Please use the {@link sonia.scm.store.BlobStoreFactory } and the {@link sonia.scm.store.BlobStore} classes to store binary files<br>
 * Please use the {@link sonia.scm.store.ConfigurationStoreFactory} and the {@link sonia.scm.store.ConfigurationStore} classes  to store configurations
 *
 * @since 2.0.0
 */
@Singleton
public class PathBasedRepositoryLocationResolver extends BasicRepositoryLocationResolver<Path> {

  public static final String STORE_NAME = "repository-paths";

  private final SCMContextProvider contextProvider;
  private final InitialRepositoryLocationResolver initialRepositoryLocationResolver;

  private final PathDatabase pathDatabase;
  private final Map<String, Path> pathById;

  private final Clock clock;

  private Long creationTime;
  private Long lastModified;

  @Inject
  public PathBasedRepositoryLocationResolver(SCMContextProvider contextProvider, InitialRepositoryLocationResolver initialRepositoryLocationResolver) {
    this(contextProvider, initialRepositoryLocationResolver, Clock.systemUTC());
  }

  public PathBasedRepositoryLocationResolver(SCMContextProvider contextProvider, InitialRepositoryLocationResolver initialRepositoryLocationResolver, Clock clock) {
    super(Path.class);
    this.contextProvider = contextProvider;
    this.initialRepositoryLocationResolver = initialRepositoryLocationResolver;
    this.pathById = new ConcurrentHashMap<>();

    this.clock = clock;

    this.creationTime = clock.millis();
    pathDatabase = new PathDatabase(resolveStorePath());

    read();
  }

  @Override
  protected <T> RepositoryLocationResolverInstance<T> create(Class<T> type) {
    return new RepositoryLocationResolverInstance<T>() {
      @Override
      public T getLocation(String repositoryId) {
        if (pathById.containsKey(repositoryId)) {
          return (T) contextProvider.resolve(pathById.get(repositoryId));
        } else {
          return (T) create(repositoryId);
        }
      }

      @Override
      public void setLocation(String repositoryId, T location) {
        PathBasedRepositoryLocationResolver.this.setLocation(repositoryId, (Path) location);
      }
    };
  }

  Path create(String repositoryId) {
    Path path = initialRepositoryLocationResolver.getPath(repositoryId);
    setLocation(repositoryId, path);
    Path resolvedPath = contextProvider.resolve(path);
    try {
      Files.createDirectories(resolvedPath);
    } catch (IOException e) {
      throw new InternalRepositoryException(entity("Repository", repositoryId), "could not create directory for new repository", e);
    }
    return resolvedPath;
  }

  Path remove(String repositoryId) {
    Path removedPath = pathById.remove(repositoryId);
    writePathDatabase();
    return contextProvider.resolve(removedPath);
  }

  void forAllPaths(BiConsumer<String, Path> consumer) {
    pathById.forEach((id, path) -> consumer.accept(id, contextProvider.resolve(path)));
  }

  void updateModificationDate() {
    this.writePathDatabase();
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
    pathById.put(id, repositoryPath);
  }

  private Path resolveStorePath() {
    return contextProvider.getBaseDirectory()
      .toPath()
      .resolve(StoreConstants.CONFIG_DIRECTORY_NAME)
      .resolve(STORE_NAME.concat(StoreConstants.FILE_EXTENSION));
  }

  public void setLocation(String repositoryId, Path repositoryBasePath) {
    pathById.put(repositoryId, repositoryBasePath);
    writePathDatabase();
  }
}
