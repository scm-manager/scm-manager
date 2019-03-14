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


package sonia.scm.repository;

import com.github.sdorra.ssp.PermissionActionCheck;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.concurrent.SubjectAwareExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ConfigurationException;
import sonia.scm.HandlerEventType;
import sonia.scm.ManagerDaoAdapter;
import sonia.scm.NotFoundException;
import sonia.scm.SCMContextProvider;
import sonia.scm.Type;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.KeyGenerator;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.CollectionAppender;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

import javax.inject.Provider;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static sonia.scm.AlreadyExistsException.alreadyExists;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

/**
 * Default implementation of {@link RepositoryManager}.
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultRepositoryManager extends AbstractRepositoryManager {

  private static final String THREAD_NAME = "Hook-%s";
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultRepositoryManager.class);
  private final ScmConfiguration configuration;
  private final ExecutorService executorService;
  private final Map<String, RepositoryHandler> handlerMap;
  private final KeyGenerator keyGenerator;
  private final RepositoryDAO repositoryDAO;
  private final Set<Type> types;
  private final Provider<NamespaceStrategy> namespaceStrategyProvider;
  private final ManagerDaoAdapter<Repository> managerDaoAdapter;


  @Inject
  public DefaultRepositoryManager(ScmConfiguration configuration,
                                  SCMContextProvider contextProvider, KeyGenerator keyGenerator,
                                  RepositoryDAO repositoryDAO, Set<RepositoryHandler> handlerSet,
                                  Provider<NamespaceStrategy> namespaceStrategyProvider) {
    this.configuration = configuration;
    this.keyGenerator = keyGenerator;
    this.repositoryDAO = repositoryDAO;
    this.namespaceStrategyProvider = namespaceStrategyProvider;

    ThreadFactory factory = new ThreadFactoryBuilder()
      .setNameFormat(THREAD_NAME).build();
    this.executorService = new SubjectAwareExecutorService(
      Executors.newCachedThreadPool(factory)
    );

    handlerMap = new HashMap<>();
    types = new HashSet<>();

    for (RepositoryHandler handler : handlerSet) {
      addHandler(contextProvider, handler);
    }
    managerDaoAdapter = new ManagerDaoAdapter<>(repositoryDAO);
  }


  @Override
  public void close() {
    executorService.shutdown();

    for (RepositoryHandler handler : handlerMap.values()) {
      IOUtil.close(handler);
    }
  }

  @Override
  public Repository create(Repository repository) {
    return create(repository, true);
  }

  public Repository create(Repository repository, boolean initRepository) {
    repository.setId(keyGenerator.createKey());
    repository.setNamespace(namespaceStrategyProvider.get().createNamespace(repository));

    logger.info("create repository {}/{} of type {} in namespace {}", repository.getNamespace(), repository.getName(), repository.getType(), repository.getNamespace());

    return managerDaoAdapter.create(
      repository,
      RepositoryPermissions::create,
      newRepository -> fireEvent(HandlerEventType.BEFORE_CREATE, newRepository),
      newRepository -> {
        fireEvent(HandlerEventType.CREATE, newRepository);
        if (initRepository) {
          try {
            getHandler(newRepository).create(newRepository);
          } catch (InternalRepositoryException e) {
            delete(repository);
            throw e;
          }
        }
      },
      newRepository -> {
        if (repositoryDAO.contains(newRepository.getNamespaceAndName())) {
          throw alreadyExists(entity(newRepository.getClass(), newRepository.getNamespaceAndName().logString()));
        }
      }
    );
  }

  @Override
  public void delete(Repository repository){
    logger.info("delete repository {}/{} of type {}", repository.getNamespace(), repository.getName(), repository.getType());
    managerDaoAdapter.delete(
      repository,
      () -> RepositoryPermissions.delete(repository),
      this::preDelete,
      toDelete -> fireEvent(HandlerEventType.DELETE, toDelete)
    );
  }

  private void preDelete(Repository toDelete) {
    if (configuration.isEnableRepositoryArchive() && !toDelete.isArchived()) {
      throw new RepositoryIsNotArchivedException();
    }
    fireEvent(HandlerEventType.BEFORE_DELETE, toDelete);
    getHandler(toDelete).delete(toDelete);
  }

  @Override
  public void importRepository(Repository repository) {
    create(repository, false);
  }

  @Override
  public void init(SCMContextProvider context) {
  }

  @Override
  public void modify(Repository repository){
    logger.info("modify repository {}/{} of type {}", repository.getNamespace(), repository.getName(), repository.getType());

    managerDaoAdapter.modify(
      repository,
      RepositoryPermissions::modify,
      notModified -> {
        fireEvent(HandlerEventType.BEFORE_MODIFY, repository, notModified);
        try {
          getHandler(repository).modify(repository);
        } catch (NotFoundException e) {
          throw new IllegalStateException("repository not found though just created", e);
        }
      },
      notModified -> fireEvent(HandlerEventType.MODIFY, repository, notModified)
    );
  }

  @Override
  public void refresh(Repository repository) {
    AssertUtil.assertIsNotNull(repository);
    RepositoryPermissions.read(repository).check();

    Repository fresh = repositoryDAO.get(repository.getNamespaceAndName());

    if (fresh != null) {
      fresh.copyProperties(repository);
    } else {
      throw notFound(entity(repository));
    }
  }


  @Override
  public Repository get(String id) {
    AssertUtil.assertIsNotEmpty(id);

    RepositoryPermissions.read(id).check();

    Repository repository = repositoryDAO.get(id);

    if (repository != null) {
      repository = repository.clone();
    }

    return repository;
  }

  @Override
  public Repository get(NamespaceAndName namespaceAndName) {
    AssertUtil.assertIsNotNull(namespaceAndName);
    AssertUtil.assertIsNotEmpty(namespaceAndName.getNamespace());
    AssertUtil.assertIsNotEmpty(namespaceAndName.getName());

    Repository repository = repositoryDAO.get(namespaceAndName);

    if (repository != null) {
      RepositoryPermissions.read(repository).check();
      repository = repository.clone();
    }

    return repository;
  }

  @Override
  public Collection<Repository> getAll(Comparator<Repository> comparator) {
    List<Repository> repositories = Lists.newArrayList();

    PermissionActionCheck<Repository> check = RepositoryPermissions.read();

    for (Repository repository : repositoryDAO.getAll()) {
      if (handlerMap.containsKey(repository.getType())
        && check.isPermitted(repository)) {
        Repository r = repository.clone();

        repositories.add(r);
      }
    }

    if (comparator != null) {
      Collections.sort(repositories, comparator);
    }

    return repositories;
  }

  @Override
  public Collection<Repository> getAll() {
    return getAll(null);
  }


  @Override
  public Collection<Repository> getAll(Comparator<Repository> comparator,
                                       int start, int limit) {
    final PermissionActionCheck<Repository> check =
      RepositoryPermissions.read();

    return Util.createSubCollection(repositoryDAO.getAll(), comparator,
      new CollectionAppender<Repository>() {
        @Override
        public void append(Collection<Repository> collection, Repository item) {
          if (check.isPermitted(item)) {
            collection.add(item.clone());
          }
        }
      }, start, limit);
  }

  @Override
  public Collection<Repository> getAll(int start, int limit) {
    return getAll(null, start, limit);
  }

  @Override
  public Collection<RepositoryType> getConfiguredTypes() {
    List<RepositoryType> validTypes = Lists.newArrayList();

    for (RepositoryHandler handler : handlerMap.values()) {
      if (handler.isConfigured()) {
        validTypes.add(handler.getType());
      }
    }

    return validTypes;
  }

  @Override
  public RepositoryHandler getHandler(String type) {
    return handlerMap.get(type);
  }

  @Override
  public Long getLastModified() {
    return repositoryDAO.getLastModified();
  }

  @Override
  public Collection<Type> getTypes() {
    return types;
  }

  private void addHandler(SCMContextProvider contextProvider,
                          RepositoryHandler handler) {
    AssertUtil.assertIsNotNull(handler);

    Type type = handler.getType();

    AssertUtil.assertIsNotNull(type);

    if (handlerMap.containsKey(type.getName())) {
      throw new ConfigurationException(
        type.getName().concat("already registered"));
    }

    if (logger.isInfoEnabled()) {
      logger.info("added RepositoryHandler {} for type {}", handler.getClass(),
        type);
    }

    handlerMap.put(type.getName(), handler);
    handler.init(contextProvider);
    types.add(type);
  }

  private RepositoryHandler getHandler(Repository repository)
    {
    String type = repository.getType();
    RepositoryHandler handler = handlerMap.get(type);

    if (handler == null) {
      throw new InternalRepositoryException(entity(repository), "could not find handler for " + type);
    } else if (!handler.isConfigured()) {
      throw new InternalRepositoryException(entity(repository), "handler is not configured for type " + type);
    }

    return handler;
  }
}
