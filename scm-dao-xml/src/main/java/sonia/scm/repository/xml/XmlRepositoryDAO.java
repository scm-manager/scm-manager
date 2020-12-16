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

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.store.StoreReadOnlyException;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sebastian Sdorra
 */
@Singleton
public class XmlRepositoryDAO implements RepositoryDAO {


  private final MetadataStore metadataStore = new MetadataStore();

  private final PathBasedRepositoryLocationResolver repositoryLocationResolver;
  private final FileSystem fileSystem;

  private final Map<String, Repository> byId;
  private final Map<NamespaceAndName, Repository> byNamespaceAndName;

  @Inject
  public XmlRepositoryDAO(PathBasedRepositoryLocationResolver repositoryLocationResolver, FileSystem fileSystem) {
    this.repositoryLocationResolver = repositoryLocationResolver;
    this.fileSystem = fileSystem;

    this.byId = new ConcurrentHashMap<>();
    this.byNamespaceAndName = new ConcurrentHashMap<>();

    init();
  }

  private void init() {
    RepositoryLocationResolver.RepositoryLocationResolverInstance<Path> pathRepositoryLocationResolverInstance = repositoryLocationResolver.create(Path.class);
    pathRepositoryLocationResolverInstance.forAllLocations((repositoryId, repositoryPath) -> {
      Repository repository = metadataStore.read(repositoryPath);
      byNamespaceAndName.put(repository.getNamespaceAndName(), repository);
      byId.put(repositoryId, repository);
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

    byId.put(repository.getId(), clone);
    byNamespaceAndName.put(repository.getNamespaceAndName(), clone);
  }

  @Override
  public boolean contains(Repository repository) {
    return byId.containsKey(repository.getId());
  }

  @Override
  public boolean contains(NamespaceAndName namespaceAndName) {
    return byNamespaceAndName.containsKey(namespaceAndName);
  }

  @Override
  public boolean contains(String id) {
    return byId.containsKey(id);
  }

  @Override
  public Repository get(NamespaceAndName namespaceAndName) {
    return byNamespaceAndName.get(namespaceAndName);
  }

  @Override
  public Repository get(String id) {
    return byId.get(id);
  }

  @Override
  public Collection<Repository> getAll() {
    return ImmutableList.copyOf(byNamespaceAndName.values());
  }

  @Override
  public void modify(Repository repository) {
    Repository clone = repository.clone();
    if (clone.isArchived() && byId.get(clone.getId()).isArchived()) {
      throw new StoreReadOnlyException(repository);
    }

    synchronized (this) {
      // remove old namespaceAndName from map, in case of rename
      Repository prev = byId.put(clone.getId(), clone);
      if (prev != null) {
        byNamespaceAndName.remove(prev.getNamespaceAndName());
      }
      byNamespaceAndName.put(clone.getNamespaceAndName(), clone);
    }

    Path repositoryPath = repositoryLocationResolver
      .create(Path.class)
      .getLocation(repository.getId());
    repositoryLocationResolver.updateModificationDate();
    metadataStore.write(repositoryPath, clone);
  }

  @Override
  public void delete(Repository repository) {
    if (repository.isArchived()) {
      throw new StoreReadOnlyException(repository);
    }
    Path path;
    synchronized (this) {
      Repository prev = byId.remove(repository.getId());
      if (prev != null) {
        byNamespaceAndName.remove(prev.getNamespaceAndName());
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
    byNamespaceAndName.clear();
    byId.clear();
    init();
  }
}
