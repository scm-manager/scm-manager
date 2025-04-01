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

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryExportingCheck;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver.DownForMaintenanceContext;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver.UpAfterMaintenanceContext;
import sonia.scm.store.StoreReadOnlyException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@Singleton
@Slf4j
public class XmlRepositoryDAO implements RepositoryDAO {

  private final MetadataStore metadataStore = new MetadataStore();

  private final PathBasedRepositoryLocationResolver repositoryLocationResolver;
  private final FileSystem fileSystem;
  private final RepositoryExportingCheck repositoryExportingCheck;

  private final Map<String, Repository> byId;
  private final Map<NamespaceAndName, Repository> byNamespaceAndName;
  private final ReadWriteLock byNamespaceLock = new ReentrantReadWriteLock();

  @Inject
  public XmlRepositoryDAO(PathBasedRepositoryLocationResolver repositoryLocationResolver, FileSystem fileSystem, RepositoryExportingCheck repositoryExportingCheck) {
    this.repositoryLocationResolver = repositoryLocationResolver;
    this.fileSystem = fileSystem;
    this.repositoryExportingCheck = repositoryExportingCheck;

    this.byId = new HashMap<>();
    this.byNamespaceAndName = new TreeMap<>();

    init();

    this.repositoryLocationResolver.registerMaintenanceCallback(new PathBasedRepositoryLocationResolver.MaintenanceCallback() {
      @Override
      public void downForMaintenance(DownForMaintenanceContext context) {
        Repository repository = byId.get(context.getRepositoryId());
        byNamespaceAndName.remove(repository.getNamespaceAndName());
        byId.remove(context.getRepositoryId());
      }

      @Override
      public void upAfterMaintenance(UpAfterMaintenanceContext context) {
        Repository repository = metadataStore.read(context.getLocation());
        byNamespaceAndName.put(repository.getNamespaceAndName(), repository);
        byId.put(context.getRepositoryId(), repository);
      }
    });
  }

  private void init() {
    withWriteLockedMaps(() -> {
      RepositoryLocationResolver.RepositoryLocationResolverInstance<Path> pathRepositoryLocationResolverInstance = repositoryLocationResolver.create(Path.class);
      pathRepositoryLocationResolverInstance.forAllLocations((repositoryId, repositoryPath) -> {
        try {
          Repository repository = metadataStore.read(repositoryPath);
          if (byNamespaceAndName.containsKey(repository.getNamespaceAndName())) {
            log.warn("Duplicate repository found. Adding suffix DUPLICATE to repository {}", repository);
            repository.setName(repository.getName() + "-" + repositoryId + "-DUPLICATE");
          }
          byNamespaceAndName.put(repository.getNamespaceAndName(), repository);
          byId.put(repositoryId, repository);
        } catch (InternalRepositoryException e) {
          log.error("could not read repository metadata from {}", repositoryPath, e);
        }
      });
    });
  }

  @Override
  public String getType() {
    return "xml";
  }

  @Override
  public synchronized void add(Repository repository) {
    add(repository, repositoryLocationResolver.create(repository));
  }

  public synchronized void add(Repository repository, Object location) {
    if (!(location instanceof Path)) {
      throw new IllegalArgumentException("can only handle locations of type " + Path.class.getName() + ", not of type " + location.getClass().getName());
    }
    Path repositoryPath = (Path) location;

    Repository clone = repository.clone();

    try {
      metadataStore.write(repositoryPath, repository);
    } catch (Exception e) {
      repositoryLocationResolver.remove(repository.getId());
      throw new InternalRepositoryException(repository, "failed to create filesystem", e);
    }

    withWriteLockedMaps(() -> {
      byId.put(repository.getId(), clone);
      byNamespaceAndName.put(repository.getNamespaceAndName(), clone);
    });
  }

  @Override
  public boolean contains(Repository repository) {
    return withReadLockedMaps(() -> byId.containsKey(repository.getId()));
  }

  @Override
  public boolean contains(NamespaceAndName namespaceAndName) {
    return withReadLockedMaps(() -> byNamespaceAndName.containsKey(namespaceAndName));
  }

  @Override
  public boolean contains(String id) {
    return withReadLockedMaps(() -> byId.containsKey(id));
  }

  @Override
  public Repository get(NamespaceAndName namespaceAndName) {
    return withReadLockedMaps(() -> byNamespaceAndName.get(namespaceAndName));
  }

  @Override
  public Repository get(String id) {
    return withReadLockedMaps(() -> byId.get(id));
  }

  @Override
  public Collection<Repository> getAll() {
    return withReadLockedMaps(() -> ImmutableList.copyOf(byNamespaceAndName.values()));
  }

  @Override
  public void modify(Repository repository) {
    Repository clone = repository.clone();
    if (mustNotModifyRepository(clone)) {
      throw new StoreReadOnlyException(repository);
    }

    withWriteLockedMaps(() -> {
      // remove old namespaceAndName from map, in case of rename
      Repository prev = byId.put(clone.getId(), clone);
      if (prev != null) {
        byNamespaceAndName.remove(prev.getNamespaceAndName());
      }
      byNamespaceAndName.put(clone.getNamespaceAndName(), clone);
    });

    Path repositoryPath = repositoryLocationResolver
      .create(Path.class)
      .getLocation(repository.getId());
    repositoryLocationResolver.updateModificationDate();
    metadataStore.write(repositoryPath, clone);
  }

  private boolean mustNotModifyRepository(Repository clone) {
    return withReadLockedMaps(() ->
      clone.isArchived() && byId.get(clone.getId()).isArchived()
        || repositoryExportingCheck.isExporting(clone)
    );
  }

  @Override
  public void delete(Repository repository) {
    if (repository.isArchived() || repositoryExportingCheck.isExporting(repository)) {
      throw new StoreReadOnlyException(repository);
    }
    Path path = withWriteLockedMaps(() -> {
      Repository prev = byId.remove(repository.getId());
      if (prev != null) {
        byNamespaceAndName.remove(prev.getNamespaceAndName());
      }
      return repositoryLocationResolver.remove(repository.getId());
    });

    try {
      fileSystem.destroy(path.toFile());
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "failed to destroy filesystem", e);
    }
  }

  @Override
  public Long getCreationTime() {
    return repositoryLocationResolver.getCreationTime();
  }

  @Override
  public Long getLastModified() {
    return repositoryLocationResolver.getLastModified();
  }

  public void refresh() {
    repositoryLocationResolver.refresh();
    withWriteLockedMaps(() -> {
      byNamespaceAndName.clear();
      byId.clear();
    });
    init();
  }

  private void withWriteLockedMaps(Runnable runnable) {
    Lock lock = byNamespaceLock.writeLock();
    lock.lock();
    try {
      runnable.run();
    } finally {
      lock.unlock();
    }
  }

  private <T> T withWriteLockedMaps(Supplier<T> runnable) {
    Lock lock = byNamespaceLock.writeLock();
    lock.lock();
    try {
      return runnable.get();
    } finally {
      lock.unlock();
    }
  }

  private <T> T withReadLockedMaps(Supplier<T> runnable) {
    Lock lock = byNamespaceLock.readLock();
    lock.lock();
    try {
      return runnable.get();
    } finally {
      lock.unlock();
    }
  }
}
