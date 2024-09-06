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

package sonia.scm.repository;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.AlreadyExistsException;
import sonia.scm.ConfigurationException;
import sonia.scm.HandlerEventType;
import sonia.scm.ManagerDaoAdapter;
import sonia.scm.NoChangesMadeException;
import sonia.scm.NotFoundException;
import sonia.scm.SCMContextProvider;
import sonia.scm.Type;
import sonia.scm.auditlog.Auditor;
import sonia.scm.event.ScmEventBus;
import sonia.scm.security.AuthorizationChangedEvent;
import sonia.scm.security.KeyGenerator;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Collections.emptySet;
import static sonia.scm.AlreadyExistsException.alreadyExists;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

/**
 * Default implementation of {@link RepositoryManager}.
 *
 */
@Singleton
public class DefaultRepositoryManager extends AbstractRepositoryManager {

  @SuppressWarnings("unchecked")
  public static final Collector<String, Object, Collection<String>> LINKED_HASH_SET_COLLECTOR = new Collector<>() {
    @Override
    public Supplier<Object> supplier() {
      return LinkedHashSet::new;
    }

    @Override
    public BiConsumer<Object, String> accumulator() {
      return (collection, value) -> ((Collection<String>) collection).add(value);
    }

    @Override
    public BinaryOperator<Object> combiner() {
      return (c1, c2) -> ((Collection<String>) c1).addAll((Collection<String>) c2);
    }

    @Override
    public Function<Object, Collection<String>> finisher() {
      return collection -> (Collection<String>) collection;
    }

    @Override
    public Set<Characteristics> characteristics() {
      return emptySet();
    }
  };
  private static final Logger LOG = LoggerFactory.getLogger(DefaultRepositoryManager.class);
  private final Map<String, RepositoryHandler> handlerMap;
  private final KeyGenerator keyGenerator;
  private final RepositoryDAO repositoryDAO;
  private final Set<Type> types;
  private final Provider<NamespaceStrategy> namespaceStrategyProvider;
  private final ManagerDaoAdapter<Repository> managerDaoAdapter;
  private final RepositoryPostProcessor repositoryPostProcessor;

  @Inject
  public DefaultRepositoryManager(SCMContextProvider contextProvider,
                                  KeyGenerator keyGenerator,
                                  RepositoryDAO repositoryDAO,
                                  Set<RepositoryHandler> handlerSet,
                                  Provider<NamespaceStrategy> namespaceStrategyProvider,
                                  RepositoryPostProcessor repositoryPostProcessor,
                                  Set<Auditor> auditors) {
    this.keyGenerator = keyGenerator;
    this.repositoryDAO = repositoryDAO;
    this.namespaceStrategyProvider = namespaceStrategyProvider;
    this.repositoryPostProcessor = repositoryPostProcessor;

    handlerMap = new HashMap<>();
    types = new HashSet<>();

    for (RepositoryHandler handler : handlerSet) {
      addHandler(contextProvider, handler);
    }
    managerDaoAdapter = new ManagerDaoAdapter<>(repositoryDAO, auditors);
  }

  @Override
  public void close() {
    for (RepositoryHandler handler : handlerMap.values()) {
      IOUtil.close(handler);
    }
  }

  @Override
  public Repository create(Repository repository) {
    return create(repository, r -> {
    }, true);
  }

  @Override
  public Repository create(Repository repository, Consumer<Repository> afterCreation) {
    return create(repository, afterCreation, true);
  }

  public Repository create(Repository repository, Consumer<Repository> afterCreation, boolean initRepository) {
    if (!isSupportedType(repository.getType())) {
      throw InvalidRepositoryTypeException.create(repository);
    }
    repository.setId(keyGenerator.createKey());
    repository.setNamespace(namespaceStrategyProvider.get().createNamespace(repository));

    LOG.info("create repository {} of type {}", repository, repository.getType());

    return managerDaoAdapter.create(
      repository,
      RepositoryPermissions::create,
      newRepository -> fireEvent(HandlerEventType.BEFORE_CREATE, newRepository),
      newRepository -> {
        if (initRepository) {
          try {
            getHandler(newRepository).create(newRepository);
            invalidateRepositoryPermissions();
            afterCreation.accept(newRepository);
          } catch (Exception e) {
            delete(repository);
            throw e;
          }
        } else {
          invalidateRepositoryPermissions();
          afterCreation.accept(newRepository);
        }
        fireEvent(HandlerEventType.CREATE, newRepository);
      },
      newRepository -> {
        if (repositoryDAO.contains(newRepository.getNamespaceAndName())) {
          throw alreadyExists(entity(newRepository.getNamespaceAndName()));
        }
      }
    );
  }

  private boolean isSupportedType(String type) {
    return getConfiguredTypes()
      .stream().anyMatch(t -> t.getName().equals(type));
  }

  private void invalidateRepositoryPermissions() {
    ScmEventBus.getInstance().post(AuthorizationChangedEvent.createForEveryUser());
  }

  @Override
  public void delete(Repository repository) {
    LOG.info("delete repository {} of type {}", repository, repository.getType());
    managerDaoAdapter.delete(
      repository,
      () -> RepositoryPermissions.delete(repository),
      this::preDelete,
      toDelete -> fireEvent(HandlerEventType.DELETE, toDelete)
    );
  }

  private void preDelete(Repository toDelete) {
    fireEvent(HandlerEventType.BEFORE_DELETE, toDelete);
    getHandler(toDelete).delete(toDelete);
  }

  @Override
  public void importRepository(Repository repository) {
    create(repository, r -> {
    }, false);
  }

  @Override
  public void init(SCMContextProvider context) {
    // Do nothing
  }

  @Override
  public void modify(Repository repository) {
    LOG.info("modify repository {} of type {}", repository, repository.getType());

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
      repository = postProcess(repository);
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
      repository = postProcess(repository);
    }

    return repository;
  }

  public Repository rename(Repository repository, String newNamespace, String newName) {

    NamespaceAndName newNamespaceAndName = new NamespaceAndName(newNamespace, newName);

    if (hasNamespaceOrNameNotChanged(repository, newNamespace, newName)) {
      throw new NoChangesMadeException(repository);
    }

    if (this.get(newNamespaceAndName) != null){
      throw AlreadyExistsException.alreadyExists(entity(NamespaceAndName.class, newNamespaceAndName.logString()));
    }

    Repository changedRepository = repository.clone();
    if (!Strings.isNullOrEmpty(newName)) {
      changedRepository.setName(newName);
    }

    if (!Strings.isNullOrEmpty(newNamespace) && !repository.getNamespace().equals(newNamespace)) {
      NamespaceStrategy strategy = namespaceStrategyProvider.get();
      if (!strategy.canBeChanged()) {
        throw new ChangeNamespaceNotAllowedException(repository);
      }
      changedRepository.setNamespace(newNamespace);
      changedRepository.setNamespace(strategy.createNamespace(changedRepository));
    }

    managerDaoAdapter.modify(
      changedRepository,
      RepositoryPermissions::rename,
      notModified -> {
      },
      notModified -> fireEvent(HandlerEventType.MODIFY, changedRepository, repository));

    return changedRepository;
  }

  @Override
  public void archive(Repository repository) {
    setArchived(repository, true);
  }

  @Override
  public void unarchive(Repository repository) {
    setArchived(repository, false);
  }

  private void setArchived(Repository repository, boolean archived) {
    Repository originalRepository = repositoryDAO.get(repository.getNamespaceAndName());

    if (archived == originalRepository.isArchived()) {
      throw new NoChangesMadeException(repository);
    }

    Repository changedRepository = postProcess(originalRepository);

    changedRepository.setArchived(archived);

    managerDaoAdapter.modify(
      changedRepository,
      RepositoryPermissions::archive,
      notModified -> {
      },
      notModified -> fireEvent(HandlerEventType.MODIFY, changedRepository, originalRepository));
  }

  private boolean hasNamespaceOrNameNotChanged(Repository repository, String newNamespace, String newName) {
    return repository.getName().equals(newName)
      && repository.getNamespace().equals(newNamespace);
  }

  @Override
  public Collection<Repository> getAll(Predicate<Repository> filter, Comparator<Repository> comparator) {
    List<Repository> repositories = Lists.newArrayList();

    for (Repository repository : repositoryDAO.getAll()) {
      if (handlerMap.containsKey(repository.getType())
        && filter.test(repository)
        && RepositoryPermissions.read().isPermitted(repository)) {
        Repository r = postProcess(repository);

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
    return getAll(repository -> true, null);
  }


  @Override
  public Collection<Repository> getAll(Comparator<Repository> comparator,
                                       int start, int limit) {

    return Util.createSubCollection(repositoryDAO.getAll(), comparator,
      (collection, item) -> {
        if (RepositoryPermissions.read().isPermitted(item)) {
          collection.add(postProcess(item));
        }
      }, start, limit);
  }

  @Override
  public Collection<Repository> getAll(int start, int limit) {
    return getAll(null, start, limit);
  }

  /**
   * @deprecated Use {@link NamespaceManager#getAll()} instead.
   */
  @Deprecated
  @Override
  public Collection<String> getAllNamespaces() {
    return getAll().stream()
      .map(Repository::getNamespace)
      .collect(LINKED_HASH_SET_COLLECTOR);
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

    LOG.info("added RepositoryHandler {} for type {}", handler.getClass(), type);

    handlerMap.put(type.getName(), handler);
    handler.init(contextProvider);
    types.add(type);
  }

  private RepositoryHandler getHandler(Repository repository) {
    String type = repository.getType();
    RepositoryHandler handler = handlerMap.get(type);

    if (handler == null) {
      throw new InternalRepositoryException(entity(repository), "could not find handler for " + type);
    } else if (!handler.isConfigured()) {
      throw new InternalRepositoryException(entity(repository), "handler is not configured for type " + type);
    }

    return handler;
  }

  private Repository postProcess(Repository repository) {
    Repository clone = repository.clone();
    repositoryPostProcessor.postProcess(repository);
    return clone;
  }
}
