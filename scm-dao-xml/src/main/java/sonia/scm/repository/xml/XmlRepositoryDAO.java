/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
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
import sonia.scm.store.StoreConstants;

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
    repositoryLocationResolver.forAllPaths((repositoryId, repositoryPath) -> {
      Path metadataPath = resolveDataPath(repositoryPath);
      Repository repository = metadataStore.read(metadataPath);
      byNamespaceAndName.put(repository.getNamespaceAndName(), repository);
      byId.put(repositoryId, repository);
    });
  }

  private Path resolveDataPath(Path repositoryPath) {
    return repositoryPath.resolve(StoreConstants.REPOSITORY_METADATA.concat(StoreConstants.FILE_EXTENSION));
  }

  @Override
  public String getType() {
    return "xml";
  }

  @Override
  public void add(Repository repository) {
    add(repository, repositoryLocationResolver.create(repository.getId()));
  }

  public void add(Repository repository, Object location) {
    if (!(location instanceof Path)) {
      throw new IllegalArgumentException("can only handle locations of type " + Path.class.getName() + ", not of type " + location.getClass().getName());
    }
    Repository clone = repository.clone();

    synchronized (this) {
      Path repositoryPath = (Path) location;

      try {
        Path metadataPath = resolveDataPath(repositoryPath);
        metadataStore.write(metadataPath, repository);
      } catch (Exception e) {
        repositoryLocationResolver.remove(repository.getId());
        throw new InternalRepositoryException(repository, "failed to create filesystem", e);
      }

      byId.put(repository.getId(), clone);
      byNamespaceAndName.put(repository.getNamespaceAndName(), clone);
    }
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
    Path metadataPath = resolveDataPath(repositoryPath);
    repositoryLocationResolver.updateModificationDate();
    metadataStore.write(metadataPath, clone);
  }

  @Override
  public void delete(Repository repository) {
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
}
