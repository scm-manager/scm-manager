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

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryExportingCheck;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.store.StoreReadOnlyException;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * @author Sebastian Sdorra
 */
@Singleton
public class XmlRepositoryDAO implements RepositoryDAO {

  private static final RepositoryContainer EMPTY_REPOSITORY_CONTAINER = new RepositoryContainer(null);

  private final MetadataStore metadataStore = new MetadataStore();

  private final PathBasedRepositoryLocationResolver repositoryLocationResolver;
  private final FileSystem fileSystem;
  private final RepositoryExportingCheck repositoryExportingCheck;

  private final Map<String, RepositoryContainer> byId;
  private final Map<NamespaceAndName, RepositoryContainer> byNamespaceAndName;
  private final ReadWriteLock byNamespaceLock = new ReentrantReadWriteLock();

  @Inject
  public XmlRepositoryDAO(PathBasedRepositoryLocationResolver repositoryLocationResolver, FileSystem fileSystem, RepositoryExportingCheck repositoryExportingCheck) {
    this.repositoryLocationResolver = repositoryLocationResolver;
    this.fileSystem = fileSystem;
    this.repositoryExportingCheck = repositoryExportingCheck;

    this.byId = new ConcurrentHashMap<>();
    this.byNamespaceAndName = new TreeMap<>();

    init();
  }

  private void init() {
    withWriteLockedByNamespaceMap(() -> {
      RepositoryLocationResolver.RepositoryLocationResolverInstance<Path> pathRepositoryLocationResolverInstance = repositoryLocationResolver.create(Path.class);
      pathRepositoryLocationResolverInstance.forAllLocations((repositoryId, repositoryPath) -> {
        Repository repository = metadataStore.read(repositoryPath);
        RepositoryContainer container = new RepositoryContainer(repository);
        byNamespaceAndName.put(repository.getNamespaceAndName(), container);
        byId.put(repositoryId, container);
      });
    });
  }

  @Override
  public String getType() {
    return "xml";
  }

  @Override
  public synchronized void add(Repository repository) {
    add(repository, repositoryLocationResolver.create(repository.getId()));
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

    withWriteLockedByNamespaceMap(() -> {
      RepositoryContainer container = new RepositoryContainer(clone);
      byId.put(repository.getId(), container);
      byNamespaceAndName.put(repository.getNamespaceAndName(), container);
    });
  }

  @Override
  public boolean contains(Repository repository) {
    return byId.containsKey(repository.getId());
  }

  @Override
  public boolean contains(NamespaceAndName namespaceAndName) {
    return withReadLockedByNamespaceMap(() -> byNamespaceAndName.containsKey(namespaceAndName));
  }

  @Override
  public boolean contains(String id) {
    return byId.containsKey(id);
  }

  @Override
  public Repository get(NamespaceAndName namespaceAndName) {
    return withReadLockedByNamespaceMap(() -> byNamespaceAndName.getOrDefault(namespaceAndName, EMPTY_REPOSITORY_CONTAINER).getRepository());
  }

  @Override
  public Repository get(String id) {
    return byId.getOrDefault(id, EMPTY_REPOSITORY_CONTAINER).getRepository();
  }

  @Override
  public Collection<Repository> getAll() {
    return withReadLockedByNamespaceMap(() ->
      unmodifiableList(
        byNamespaceAndName
          .values()
          .stream()
          .map(RepositoryContainer::getRepository)
          .collect(toList())));
  }

  @Override
  public void modify(Repository repository) {
    Repository clone = repository.clone();
    if (mustNotModifyRepository(clone)) {
      throw new StoreReadOnlyException(repository);
    }

    synchronized (this) {
      // remove old namespaceAndName from map, in case of rename
      RepositoryContainer container = byId.get(clone.getId());
      if (container != null) {
        if (container.getRepository().getNamespaceAndName() != clone.getNamespaceAndName()) {
          withWriteLockedByNamespaceMap(() -> {
            byNamespaceAndName.remove(container.getRepository().getNamespaceAndName());
            byNamespaceAndName.put(clone.getNamespaceAndName(), container);
          });
        }
        container.setRepository(clone);
      } else {
        RepositoryContainer newContainer = new RepositoryContainer(clone);
        withWriteLockedByNamespaceMap(() -> byNamespaceAndName.put(clone.getNamespaceAndName(), newContainer));
        byId.put(clone.getId(), newContainer);
      }
    }

    Path repositoryPath = repositoryLocationResolver
      .create(Path.class)
      .getLocation(repository.getId());
    repositoryLocationResolver.updateModificationDate();
    metadataStore.write(repositoryPath, clone);
  }

  private boolean mustNotModifyRepository(Repository clone) {
    return clone.isArchived() && byId.get(clone.getId()) != null && byId.get(clone.getId()).getRepository().isArchived()
      || repositoryExportingCheck.isExporting(clone);
  }

  @Override
  public void delete(Repository repository) {
    if (repository.isArchived() || repositoryExportingCheck.isExporting(repository)) {
      throw new StoreReadOnlyException(repository);
    }
    Path path;
    synchronized (this) {
      RepositoryContainer prev = byId.remove(repository.getId());
      if (prev != null) {
        withWriteLockedByNamespaceMap(() -> byNamespaceAndName.remove(prev.getRepository().getNamespaceAndName()));
      }
      path = repositoryLocationResolver.remove(repository.getId());
    }

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
    withWriteLockedByNamespaceMap(byNamespaceAndName::clear);
    byId.clear();
    init();
  }

  private void withWriteLockedByNamespaceMap(Runnable runnable) {
    Lock lock = byNamespaceLock.writeLock();
    lock.lock();
    try {
      runnable.run();
    } finally {
      lock.unlock();
    }
  }

  private <T> T withReadLockedByNamespaceMap(Supplier<T> runnable) {
    Lock lock = byNamespaceLock.readLock();
    lock.lock();
    try {
      return runnable.get();
    } finally {
      lock.unlock();
    }
  }

  private static class RepositoryContainer {
    private Repository repository;

    public RepositoryContainer(Repository repository) {
      this.repository = repository;
    }

    public Repository getRepository() {
      return repository;
    }

    public void setRepository(Repository repository) {
      this.repository = repository;
    }
  }
}
