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

package sonia.scm.repository.api;


import com.github.legman.ReferenceType;
import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.HandlerEventType;
import sonia.scm.NotFoundException;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.ClearRepositoryCacheEvent;
import sonia.scm.repository.DefaultRepositoryExportingCheck;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKeyPredicate;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryExportingCheck;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.spi.RepositoryServiceProvider;
import sonia.scm.repository.spi.RepositoryServiceResolver;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.security.PublicKeyCreatedEvent;
import sonia.scm.security.PublicKeyDeletedEvent;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.user.EMail;

import java.util.Set;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

/**
 * The {@link RepositoryServiceFactory} is the entrypoint of the repository api.
 * You could create {@link RepositoryService} from a {@link Repository} and
 * with the {@link RepositoryService} you could browse and control the entire
 * {@link Repository}.
 * <p>&nbsp;</p>
 * <b>Simple usage example:</b>
 *
 * <pre><code>
 * public class Sample {
 *
 *   private final RepositoryServiceFactory factory;
 *
 *   {@literal @}Inject
 *   public Sample(RepositoryServiceFactory factory){
 *     this.factory = factory;
 *   }
 *
 *   public Changeset getChangeset(String repositoryId, String commitId){
 *     Changeset changeset = null;
 *     RepositoryService service = null;
 *     try {
 *       service = factory.create(repositoryId);
 *       changeset = service.getLogCommand().getChangeset(commitId);
 *     } finally {
 *       if ( service != null ){
 *         service.close();
 *       }
 *     }
 *     return changeset;
 *   }
 *
 * }
 * </code></pre>
 *
 * @since 1.17
 */
@Singleton
public final class RepositoryServiceFactory {

 
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryServiceFactory.class);

  private final CacheManager cacheManager;
  private final RepositoryManager repositoryManager;
  private final Set<RepositoryServiceResolver> resolvers;
  private final PreProcessorUtil preProcessorUtil;
  @SuppressWarnings({"rawtypes", "java:S3740"})
  private final Set<ScmProtocolProvider> protocolProviders;
  private final WorkdirProvider workdirProvider;
  private final RepositoryExportingCheck repositoryExportingCheck;

  @Nullable
  private final EMail eMail;


  /**
   * Constructs a new {@link RepositoryServiceFactory}. This constructor
   * should not be called manually, it should only be used by the injection
   * container.
   *
   * @param configuration     configuration
   * @param cacheManager      cache manager
   * @param repositoryManager manager for repositories
   * @param resolvers         a set of {@link RepositoryServiceResolver}
   * @param preProcessorUtil  helper object for pre processor handling
   * @param protocolProviders providers for repository protocols
   * @param workdirProvider   provider for working directories
   *
   * @deprecated use {@link RepositoryServiceFactory#RepositoryServiceFactory(CacheManager, RepositoryManager, Set, PreProcessorUtil, Set, WorkdirProvider, EMail, RepositoryExportingCheck)} instead
   * @since 1.21
   */
  @Deprecated
  public RepositoryServiceFactory(ScmConfiguration configuration,
                                  CacheManager cacheManager, RepositoryManager repositoryManager,
                                  Set<RepositoryServiceResolver> resolvers, PreProcessorUtil preProcessorUtil,
                                  @SuppressWarnings({"rawtypes", "java:S3740"}) Set<ScmProtocolProvider> protocolProviders,
                                  WorkdirProvider workdirProvider) {
    this(
      cacheManager, repositoryManager, resolvers,
      preProcessorUtil, protocolProviders, workdirProvider, null, ScmEventBus.getInstance(),
      new DefaultRepositoryExportingCheck()
    );
  }

  /**
   * Constructs a new {@link RepositoryServiceFactory}. This constructor
   * should not be called manually, it should only be used by the injection
   * container.
   *
   * @param cacheManager      cache manager
   * @param repositoryManager manager for repositories
   * @param resolvers         a set of {@link RepositoryServiceResolver}
   * @param preProcessorUtil  helper object for pre processor handling
   * @param protocolProviders providers for repository protocols
   * @param workdirProvider   provider for working directories
   * @param eMail             handling user emails
   * @since 2.8.0
   */
  @Inject
  public RepositoryServiceFactory(CacheManager cacheManager, RepositoryManager repositoryManager,
                                  Set<RepositoryServiceResolver> resolvers, PreProcessorUtil preProcessorUtil,
                                  @SuppressWarnings({"rawtypes", "java:S3740"})  Set<ScmProtocolProvider> protocolProviders,
                                  WorkdirProvider workdirProvider, EMail eMail,
                                  RepositoryExportingCheck repositoryExportingCheck) {
    this(
      cacheManager, repositoryManager, resolvers,
      preProcessorUtil, protocolProviders, workdirProvider,
      eMail, ScmEventBus.getInstance(), repositoryExportingCheck
    );
  }

  @VisibleForTesting
  @SuppressWarnings("java:S107") // to keep backward compatibility, we can not reduce amount of parameters
  RepositoryServiceFactory(CacheManager cacheManager, RepositoryManager repositoryManager,
                           Set<RepositoryServiceResolver> resolvers, PreProcessorUtil preProcessorUtil,
                           @SuppressWarnings({"rawtypes", "java:S3740"}) Set<ScmProtocolProvider> protocolProviders,
                           WorkdirProvider workdirProvider, @Nullable EMail eMail, ScmEventBus eventBus,
                           RepositoryExportingCheck repositoryExportingCheck) {
    this.cacheManager = cacheManager;
    this.repositoryManager = repositoryManager;
    this.resolvers = resolvers;
    this.preProcessorUtil = preProcessorUtil;
    this.protocolProviders = protocolProviders;
    this.workdirProvider = workdirProvider;
    this.eMail = eMail;
    this.repositoryExportingCheck = repositoryExportingCheck;

    eventBus.register(new CacheClearHook(cacheManager));
  }

  /**
   * Creates a new RepositoryService for the given repository.
   *
   * @param repositoryId id of the repository
   * @return a implementation of RepositoryService
   * for the given type of repository
   * @throws NotFoundException                  if no repository
   *                                            with the given id is available
   * @throws RepositoryServiceNotFoundException if no repository service
   *                                            implementation for this kind of repository is available
   * @throws IllegalArgumentException           if the repository id is null or empty
   * @throws ScmSecurityException               if current user has not read permissions
   *                                            for that repository
   */
  public RepositoryService create(String repositoryId) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(repositoryId),
      "a non empty repositoryId is required");

    Repository repository = repositoryManager.get(repositoryId);

    if (repository == null) {
      throw new NotFoundException(Repository.class, repositoryId);
    }

    return create(repository);
  }

  /**
   * Creates a new RepositoryService for the given repository.
   *
   * @param namespaceAndName namespace and name of the repository
   * @return a implementation of RepositoryService
   * for the given type of repository
   * @throws NotFoundException                  if no repository
   *                                            with the given id is available
   * @throws RepositoryServiceNotFoundException if no repository service
   *                                            implementation for this kind of repository is available
   * @throws IllegalArgumentException           if one of the parameters is null or empty
   * @throws ScmSecurityException               if current user has not read permissions
   *                                            for that repository
   */
  public RepositoryService create(NamespaceAndName namespaceAndName) {
    Preconditions.checkArgument(namespaceAndName != null,
      "a non empty namespace and name is required");

    Repository repository = repositoryManager.get(namespaceAndName);

    if (repository == null) {
      throw notFound(entity(namespaceAndName));
    }

    return create(repository);
  }

  /**
   * Creates a new RepositoryService for the given repository.
   *
   * @param repository the repository
   * @return a implementation of RepositoryService
   * for the given type of repository
   * @throws RepositoryServiceNotFoundException if no repository service
   *                                            implementation for this kind of repository is available
   * @throws NullPointerException               if the repository is null
   * @throws ScmSecurityException               if current user has not read permissions
   *                                            for that repository
   */
  public RepositoryService create(Repository repository) {
    Preconditions.checkNotNull(repository, "repository is required");

    // check for read permissions of current user
    RepositoryPermissions.read(repository).check();

    RepositoryService service = null;

    for (RepositoryServiceResolver resolver : resolvers) {
      RepositoryServiceProvider provider = resolver.resolve(repository);

      if (provider != null) {
        if (logger.isDebugEnabled()) {
          logger.debug(
            "create new repository service for repository {} of type {}",
            repository.getName(), repository.getType());
        }

        service = new RepositoryService(cacheManager, provider, repository,
          preProcessorUtil, protocolProviders, workdirProvider, eMail, repositoryExportingCheck);

        break;
      }
    }

    if (service == null) {
      throw new RepositoryServiceNotFoundException(repository);
    }

    return service;
  }

  /**
   * Hook and listener to clear all relevant repository caches.
   */
  private static class CacheClearHook {

    private final Set<Cache<?, ?>> caches = Sets.newHashSet();
    private final CacheManager cacheManager;

    /**
     * Constructs a new instance and collect all repository relevant
     * caches from the {@link CacheManager}.
     *
     * @param cacheManager cache manager
     */
    public CacheClearHook(CacheManager cacheManager) {
      this.cacheManager = cacheManager;
      this.caches.add(cacheManager.getCache(BlameCommandBuilder.CACHE_NAME));
      this.caches.add(cacheManager.getCache(BrowseCommandBuilder.CACHE_NAME));
      this.caches.add(cacheManager.getCache(LogCommandBuilder.CACHE_NAME));
      this.caches.add(cacheManager.getCache(TagsCommandBuilder.CACHE_NAME));
      this.caches.add(cacheManager.getCache(BranchesCommandBuilder.CACHE_NAME));
      this.caches.add(cacheManager.getCache(BranchDetailsCommandBuilder.CACHE_NAME));
    }

    /**
     * Clear caches on explicit repository cache clear event.
     *
     * @param event clear event
     */
    @Subscribe
    public void onEvent(ClearRepositoryCacheEvent event) {
      clearCaches(event.getRepository().getId());
    }

    /**
     * Clear caches on repository push.
     * We do this synchronously, because there are often workflows which are creating branches and fetch them straight
     * after the creation.
     *
     * @param event hook event
     */
    @Subscribe(async = false, referenceType = ReferenceType.STRONG)
    public void onEvent(PostReceiveRepositoryHookEvent event) {
      Repository repository = event.getRepository();

      if (repository != null) {
        String id = repository.getId();

        clearCaches(id);
      }
    }

    /**
     * Clear caches on repository delete event.
     *
     * @param event repository event
     */
    @Subscribe(referenceType = ReferenceType.STRONG)
    public void onEvent(RepositoryEvent event) {
      if (event.getEventType() == HandlerEventType.DELETE) {
        clearCaches(event.getItem().getId());
      }
    }

    @Subscribe
    public void onEvent(PublicKeyDeletedEvent event) {
      invalidateCachesForChangedPublicKeys();
    }

    @Subscribe
    public void onEvent(PublicKeyCreatedEvent event) {
      invalidateCachesForChangedPublicKeys();
    }

    private void invalidateCachesForChangedPublicKeys() {
      cacheManager.getCache(LogCommandBuilder.CACHE_NAME).clear();
      cacheManager.getCache(TagsCommandBuilder.CACHE_NAME).clear();
    }

    @SuppressWarnings({"unchecked", "java:S3740", "rawtypes"})
    private void clearCaches(final String repositoryId) {
      if (logger.isDebugEnabled()) {
        logger.debug("clear caches for repository id {}", repositoryId);
      }

      RepositoryCacheKeyPredicate predicate = new RepositoryCacheKeyPredicate(repositoryId);
      caches.forEach(cache -> cache.removeAll(predicate));
    }
  }

}
