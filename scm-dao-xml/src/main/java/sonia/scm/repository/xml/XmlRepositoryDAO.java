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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.NotFoundException;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.FileSystem;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.InitialRepositoryLocationResolver.InitialRepositoryLocation;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PathBasedRepositoryDAO;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.xml.AbstractXmlDAO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

/**
 * @author Sebastian Sdorra
 */
@Singleton
public class XmlRepositoryDAO
  extends AbstractXmlDAO<Repository, XmlRepositoryDatabase>
  implements PathBasedRepositoryDAO {

  public static final String STORE_NAME = "repositories";

  private InitialRepositoryLocationResolver initialRepositoryLocationResolver;
  private final FileSystem fileSystem;
  private final SCMContextProvider context;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *  @param storeFactory
   * @param fileSystem
   * @param context
   */
  @Inject
  public XmlRepositoryDAO(ConfigurationStoreFactory storeFactory, InitialRepositoryLocationResolver initialRepositoryLocationResolver, FileSystem fileSystem, SCMContextProvider context) {
    super(storeFactory.getStore(XmlRepositoryDatabase.class, STORE_NAME));
    this.initialRepositoryLocationResolver = initialRepositoryLocationResolver;
    this.fileSystem = fileSystem;
    this.context = context;
  }

  //~--- methods --------------------------------------------------------------

  @Override
  public boolean contains(NamespaceAndName namespaceAndName) {
    return db.contains(namespaceAndName);
  }

  //~--- get methods ----------------------------------------------------------

  @Override
  public Repository get(NamespaceAndName namespaceAndName) {
    return db.get(namespaceAndName);
  }

  //~--- methods --------------------------------------------------------------


  @Override
  public void modify(Repository repository) {
    RepositoryPath repositoryPath = findExistingRepositoryPath(repository).orElseThrow(() -> new InternalRepositoryException(repository, "path object for repository not found"));
    repositoryPath.setRepository(repository);
    repositoryPath.setToBeSynchronized(true);
    storeDB();
  }

  @Override
  public void add(Repository repository) {
    InitialRepositoryLocation initialLocation = initialRepositoryLocationResolver.getRelativeRepositoryPath(repository);
    try {
      fileSystem.create(initialLocation.getAbsolutePath());
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "could not create directory for repository data: " + initialLocation.getAbsolutePath(), e);
    }
    RepositoryPath repositoryPath = new RepositoryPath(initialLocation.getRelativePath(), repository.getId(), repository.clone());
    repositoryPath.setToBeSynchronized(true);
    synchronized (store) {
      db.add(repositoryPath);
      storeDB();
    }
  }

  @Override
  public Repository get(String id) {
    RepositoryPath repositoryPath = db.get(id);
    if (repositoryPath != null) {
      return repositoryPath.getRepository();
    }
    return null;
  }

  @Override
  public Collection<Repository> getAll() {
    return db.getRepositories();
  }

  /**
   * Method description
   *
   * @param repository
   * @return
   */
  @Override
  protected Repository clone(Repository repository) {
    return repository.clone();
  }

  @Override
  public void delete(Repository repository) {
    Path directory = getPath(repository);
    super.delete(repository);
    try {
        fileSystem.destroy(directory.toFile());
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "could not delete repository directory", e);
    }
  }

  /**
   * Method description
   *
   * @return
   */
  @Override
  protected XmlRepositoryDatabase createNewDatabase() {
    return new XmlRepositoryDatabase();
  }

  @Override
  public Path getPath(Repository repository) {
    return context
      .getBaseDirectory()
      .toPath()
      .resolve(
        findExistingRepositoryPath(repository)
          .map(RepositoryPath::getPath)
          .orElseThrow(() -> new InternalRepositoryException(repository, "could not find base directory for repository")));
  }

  private Optional<RepositoryPath> findExistingRepositoryPath(Repository repository) {
    return db.values().stream()
      .filter(repoPath -> repoPath.getId().equals(repository.getId()))
      .findAny();
  }
}
