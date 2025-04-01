/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.xml;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.event.EventListenerSupport;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.BasicRepositoryLocationResolver;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.Repository;
import sonia.scm.store.file.StoreConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

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

  private EventListenerSupport<MaintenanceCallback> maintenanceCallbacks = EventListenerSupport.create(MaintenanceCallback.class);

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
  @SuppressWarnings("unchecked")
  public <T> RepositoryLocationResolverInstance<T> create(Class<T> type) {
    if (type.isAssignableFrom(Path.class)) {
      return (RepositoryLocationResolverInstance<T>) new RepositoryLocationResolverInstance<Path>() {
        @Override
        public Path getLocation(String repositoryId) {
          if (pathById.containsKey(repositoryId)) {
            return contextProvider.resolve(pathById.get(repositoryId));
          } else {
            throw new LocationNotFoundException(repositoryId);
          }
        }

        @Override
        public Path createLocation(String repositoryId) {
          if (pathById.containsKey(repositoryId)) {
            throw new IllegalStateException("location for repository " + repositoryId + " already exists");
          } else {
            return create(repositoryId);
          }
        }

        @Override
        public void setLocation(String repositoryId, Path location) {
          if (pathById.containsKey(repositoryId)) {
            throw new IllegalStateException("location for repository " + repositoryId + " already exists");
          } else {
            PathBasedRepositoryLocationResolver.this.setLocation(repositoryId, location.toAbsolutePath());
          }
        }

        @Override
        public void modifyLocation(String repositoryId, Path newPath) throws RepositoryStorageException {
          modifyLocation(repositoryId, newPath, oldPath -> FileUtils.moveDirectory(contextProvider.resolve(oldPath).toFile(), newPath.toFile()));
        }

        @Override
        public void modifyLocationAndKeepOld(String repositoryId, Path newPath) throws RepositoryStorageException {
          modifyLocation(repositoryId, newPath, oldPath -> FileUtils.copyDirectory(contextProvider.resolve(oldPath).toFile(), newPath.toFile()));
        }

        private void modifyLocation(String repositoryId, Path newPath, Modifier modifier) throws RepositoryStorageException {
          if (newPath.toFile().exists() && !newPath.toFile().canWrite()) {
            throw new RepositoryStorageException("cannot create repository at new path " + newPath + "; path is not writable");
          }
          maintenanceCallbacks.fire().downForMaintenance(new DownForMaintenanceContext(repositoryId));
          Path oldPath = pathById.get(repositoryId);
          pathById.remove(repositoryId);
          try {
            modifier.modify(contextProvider.resolve(oldPath));
          } catch (Exception e) {
            PathBasedRepositoryLocationResolver.this.setLocation(repositoryId, oldPath);
            maintenanceCallbacks.fire().upAfterMaintenance(new UpAfterMaintenanceContext(repositoryId, oldPath));
            throw new RepositoryStorageException("could not create repository at new path " + newPath, e);
          }
          PathBasedRepositoryLocationResolver.this.setLocation(repositoryId, newPath);
          maintenanceCallbacks.fire().upAfterMaintenance(new UpAfterMaintenanceContext(repositoryId, newPath));
        }

        @Override
        public void forAllLocations(BiConsumer<String, Path> consumer) {
          pathById.forEach((id, path) -> consumer.accept(id, contextProvider.resolve(path)));
        }
      };
    } else {
      throw new IllegalArgumentException("type not supported: " + type);
    }
  }

  Path create(Repository repository) {
    Path path = initialRepositoryLocationResolver.getPath(repository);
    return create(repository.getId(), path);
  }

  Path create(String repositoryId) {
    Path path = initialRepositoryLocationResolver.getPath(repositoryId);
    return create(repositoryId, path);
  }

  private Path create(String repositoryId, Path path) {
    if (Files.exists(path)) {
      throw new RepositoryStorageException("path " + path + " for repository " + repositoryId + " already exists");
    }
    setLocation(repositoryId, path);
    Path resolvedPath = contextProvider.resolve(path);
    try {
      fileSystem.create(resolvedPath.toFile());
    } catch (Exception e) {
      throw new RepositoryStorageException("could not create directory " + path + " for new repository " + repositoryId, e);
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

  void registerMaintenanceCallback(MaintenanceCallback maintenanceCallback) {
    maintenanceCallbacks.addListener(maintenanceCallback);
  }

  public interface MaintenanceCallback {
    default void downForMaintenance(DownForMaintenanceContext context) {}

    default void upAfterMaintenance(UpAfterMaintenanceContext context) {}
  }

  @Getter
  @EqualsAndHashCode
  public static class DownForMaintenanceContext {
    private final String repositoryId;

    DownForMaintenanceContext(String repositoryId) {
      this.repositoryId = repositoryId;
    }
  }

  @Getter
  @EqualsAndHashCode
  public static class UpAfterMaintenanceContext {
    private final String repositoryId;
    private final Path location;

    UpAfterMaintenanceContext(String repositoryId, Path location) {
      this.repositoryId = repositoryId;
      this.location = location;
    }
  }

  private interface Modifier {
    void modify(Path oldPath) throws IOException;
  }
}
