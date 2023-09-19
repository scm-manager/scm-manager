/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.repository.xml;

import sonia.scm.SCMContextProvider;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.BasicRepositoryLocationResolver;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.store.StoreConstants;

import javax.inject.Inject;
import javax.inject.Singleton;
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
  private final FileSystem fileSystem;

  private final PathDatabase pathDatabase;
  private final Map<String, Path> pathById;

  private final Clock clock;

  private long creationTime;
  private long lastModified;

  @Inject
  public PathBasedRepositoryLocationResolver(SCMContextProvider contextProvider, InitialRepositoryLocationResolver initialRepositoryLocationResolver, FileSystem fileSystem) {
    this(contextProvider, initialRepositoryLocationResolver, fileSystem, Clock.systemUTC());
  }

  PathBasedRepositoryLocationResolver(SCMContextProvider contextProvider, InitialRepositoryLocationResolver initialRepositoryLocationResolver, FileSystem fileSystem, Clock clock) {
    super(Path.class);
    this.contextProvider = contextProvider;
    this.initialRepositoryLocationResolver = initialRepositoryLocationResolver;
    this.fileSystem = fileSystem;
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
          throw new LocationNotFoundException(repositoryId);
        }
      }

      @Override
      public T createLocation(String repositoryId) {
        if (pathById.containsKey(repositoryId)) {
          throw new IllegalStateException("location for repository " + repositoryId + " already exists");
        } else {
          return (T) create(repositoryId);
        }
      }

      @Override
      public void setLocation(String repositoryId, T location) {
        if (pathById.containsKey(repositoryId)) {
          throw new IllegalStateException("location for repository " + repositoryId + " already exists");
        } else {
          PathBasedRepositoryLocationResolver.this.setLocation(repositoryId, ((Path) location).toAbsolutePath());
        }
      }

      @Override
      public void forAllLocations(BiConsumer<String, T> consumer) {
        pathById.forEach((id, path) -> consumer.accept(id, (T) contextProvider.resolve(path)));
      }
    };
  }

  Path create(String repositoryId) {
    Path path = initialRepositoryLocationResolver.getPath(repositoryId);
    setLocation(repositoryId, path);
    Path resolvedPath = contextProvider.resolve(path);
    try {
      fileSystem.create(resolvedPath.toFile());
    } catch (Exception e) {
      throw new InternalRepositoryException(entity("Repository", repositoryId), "could not create directory for new repository", e);
    }
    return resolvedPath;
  }

  Path remove(String repositoryId) {
    Path removedPath = pathById.remove(repositoryId);
    writePathDatabase();
    return contextProvider.resolve(removedPath);
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

  private void onLoadDates(long creationTime, long lastModified) {
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

  private void setLocation(String repositoryId, Path repositoryBasePath) {
    pathById.put(repositoryId, repositoryBasePath);
    writePathDatabase();
  }

  public void refresh() {
    this.read();
  }
}
