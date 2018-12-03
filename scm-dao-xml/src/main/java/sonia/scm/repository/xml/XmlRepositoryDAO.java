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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PathBasedRepositoryDAO;
import sonia.scm.repository.Repository;
import sonia.scm.store.StoreConstants;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sebastian Sdorra
 */
@Singleton
public class XmlRepositoryDAO implements PathBasedRepositoryDAO {

  private static final String STORE_NAME = "repositories";

  private final PathDatabase pathDatabase;
  private final MetadataStore metadataStore = new MetadataStore();

  private final SCMContextProvider context;
  private final InitialRepositoryLocationResolver locationResolver;
  private final FileSystem fileSystem;

  private final Map<String, Path> pathById;
  private final Map<String, Repository> byId;
  private final Map<NamespaceAndName, Repository> byNamespaceAndName;

  private final Clock clock;

  private Long creationTime;
  private Long lastModified;

  @Inject
  public XmlRepositoryDAO(SCMContextProvider context, InitialRepositoryLocationResolver locationResolver, FileSystem fileSystem) {
    this(context, locationResolver, fileSystem, Clock.systemUTC());
  }

  XmlRepositoryDAO(SCMContextProvider context, InitialRepositoryLocationResolver locationResolver, FileSystem fileSystem, Clock clock) {
    this.context = context;
    this.locationResolver = locationResolver;
    this.fileSystem = fileSystem;

    this.clock = clock;
    this.creationTime = clock.millis();

    this.pathById = new ConcurrentHashMap<>();
    this.byId = new ConcurrentHashMap<>();
    this.byNamespaceAndName = new ConcurrentHashMap<>();

    pathDatabase = new PathDatabase(resolveStorePath());
    read();
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

  private void onLoadRepository(String id, Path repositoryPath) {
    Path metadataPath = resolveMetadataPath(context.resolve(repositoryPath));

    Repository repository = metadataStore.read(metadataPath);

    byId.put(id, repository);
    byNamespaceAndName.put(repository.getNamespaceAndName(), repository);
    pathById.put(id, repositoryPath);
  }

  @VisibleForTesting
  Path resolveStorePath() {
    return context.getBaseDirectory()
      .toPath()
      .resolve(StoreConstants.CONFIG_DIRECTORY_NAME)
      .resolve(STORE_NAME.concat(StoreConstants.FILE_EXTENSION));
  }


  @VisibleForTesting
  Path resolveMetadataPath(Path repositoryPath) {
    return repositoryPath.resolve(StoreConstants.REPOSITORY_METADATA.concat(StoreConstants.FILE_EXTENSION));
  }

  @Override
  public String getType() {
    return "xml";
  }

  @Override
  public Long getCreationTime() {
    return creationTime;
  }

  @Override
  public Long getLastModified() {
    return lastModified;
  }

  @Override
  public void add(Repository repository) {
    Repository clone = repository.clone();

    Path repositoryPath = locationResolver.getPath(repository.getId());
    Path resolvedPath = context.resolve(repositoryPath);

    try {
      fileSystem.create(resolvedPath.toFile());

      Path metadataPath = resolveMetadataPath(resolvedPath);
      metadataStore.write(metadataPath, repository);

      synchronized (this) {
        pathById.put(repository.getId(), repositoryPath);

        byId.put(repository.getId(), clone);
        byNamespaceAndName.put(repository.getNamespaceAndName(), clone);

        writePathDatabase();
      }

    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "failed to create filesystem", e);
    }
  }

  private void writePathDatabase() {
    lastModified = clock.millis();
    pathDatabase.write(creationTime, lastModified, pathById);
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

      writePathDatabase();
    }

    Path repositoryPath = context.resolve(getPath(repository.getId()));
    Path metadataPath = resolveMetadataPath(repositoryPath);
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

      path = pathById.remove(repository.getId());

      writePathDatabase();
    }

    path = context.resolve(path);

    try {
      fileSystem.destroy(path.toFile());
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "failed to destroy filesystem", e);
    }
  }

  @Override
  public Path getPath(String repositoryId) {
    return pathById.get(repositoryId);
  }
}
