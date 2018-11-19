/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
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
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.repository.xml;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PathBasedRepositoryDAO;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPathNotFoundException;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.xml.AbstractXmlDAO;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;

/**
 * @author Sebastian Sdorra
 */
@Singleton
public class XmlRepositoryDAO
  extends AbstractXmlDAO<Repository, XmlRepositoryDatabase>
  implements PathBasedRepositoryDAO {

  /**
   * Field description
   */
  public static final String STORE_NAME = "repositories";
  private final InitialRepositoryLocationResolver initialRepositoryLocationResolver;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param storeFactory
   */
  @Inject
  public XmlRepositoryDAO(ConfigurationStoreFactory storeFactory, InitialRepositoryLocationResolver initialRepositoryLocationResolver) {
    super(new SimpleStore(storeFactory.getStore(XmlRepositoryDatabasePersistence.class, STORE_NAME).get(), initialRepositoryLocationResolver));
    if (initialRepositoryLocationResolver == null) {
      throw new NullPointerException("resolver must not be null");
    }
    this.initialRepositoryLocationResolver = initialRepositoryLocationResolver;
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
    db.remove(repository.getId());
    add(repository);
  }

  @Override
  public void add(Repository repository) {
    synchronized (store) {
      db.add(repository);
      storeDB();
    }
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

  /**
   * Method description
   *
   * @return
   */
  @Override
  protected XmlRepositoryDatabase createNewDatabase() {
    return new XmlRepositoryDatabase(new XmlRepositoryDatabasePersistence(), initialRepositoryLocationResolver);
  }

  @Override
  public Path getPath(Repository repository) throws RepositoryPathNotFoundException {
    Optional<RepositoryPath> repositoryPath = db.getPaths().stream()
      .filter(repoPath -> repoPath.getId().equals(repository.getId()))
      .findFirst();
    if (!repositoryPath.isPresent()) {
      throw new RepositoryPathNotFoundException();
    } else {

      return Paths.get(repositoryPath.get().getPath());
    }
  }

  private static class SimpleStore implements ConfigurationStore<XmlRepositoryDatabase> {
    private final XmlRepositoryDatabase xmlRepositoryDatabase;

    public SimpleStore(XmlRepositoryDatabasePersistence xmlRepositoryDatabasePersistence, InitialRepositoryLocationResolver initialRepositoryLocationResolver) {
      if (xmlRepositoryDatabasePersistence == null) {
        this.xmlRepositoryDatabase = new XmlRepositoryDatabase(new XmlRepositoryDatabasePersistence(), initialRepositoryLocationResolver);
      } else {
        this.xmlRepositoryDatabase = new XmlRepositoryDatabase(xmlRepositoryDatabasePersistence, initialRepositoryLocationResolver);
      }
    }

    @Override
    public XmlRepositoryDatabase get() {
      return xmlRepositoryDatabase;
    }

    @Override
    public void set(XmlRepositoryDatabase obejct) {

    }
  }
}
