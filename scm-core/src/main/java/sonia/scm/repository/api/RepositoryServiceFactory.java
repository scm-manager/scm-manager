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

package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.ReferenceType;
import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.HandlerEventType;
import sonia.scm.NotFoundException;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.ClearRepositoryCacheEvent;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKeyPredicate;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.spi.RepositoryServiceProvider;
import sonia.scm.repository.spi.RepositoryServiceResolver;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.security.ScmSecurityException;

import java.util.Set;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

//~--- JDK imports ------------------------------------------------------------

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
 * @author Sebastian Sdorra
 * @since 1.17
 *
 * @apiviz.landmark
 * @apiviz.uses sonia.scm.repository.api.RepositoryService
 */
@Singleton
public final class RepositoryServiceFactory
{

  /**
   * the logger for RepositoryServiceFactory
   */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryServiceFactory.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link RepositoryServiceFactory}. This constructor
   * should not be called manually, it should only be used by the injection
   * container.
   *
   *
   * @param configuration configuration
   * @param cacheManager cache manager
   * @param repositoryManager manager for repositories
   * @param resolvers a set of {@link RepositoryServiceResolver}
   * @param preProcessorUtil helper object for pre processor handling
   * @param protocolProviders
   * @param workdirProvider
   * @since 1.21
   */
  @Inject
  public RepositoryServiceFactory(ScmConfiguration configuration,
                                  CacheManager cacheManager, RepositoryManager repositoryManager,
                                  Set<RepositoryServiceResolver> resolvers, PreProcessorUtil preProcessorUtil,
                                  Set<ScmProtocolProvider> protocolProviders, WorkdirProvider workdirProvider)
  {
    this(
      configuration, cacheManager, repositoryManager, resolvers,
      preProcessorUtil, protocolProviders, workdirProvider, ScmEventBus.getInstance()
    );
  }

  @VisibleForTesting
  RepositoryServiceFactory(ScmConfiguration configuration,
                                  CacheManager cacheManager, RepositoryManager repositoryManager,
                                  Set<RepositoryServiceResolver> resolvers, PreProcessorUtil preProcessorUtil,
                                  Set<ScmProtocolProvider> protocolProviders, WorkdirProvider workdirProvider,
                                  ScmEventBus eventBus)
  {
    this.configuration = configuration;
    this.cacheManager = cacheManager;
    this.repositoryManager = repositoryManager;
    this.resolvers = resolvers;
    this.preProcessorUtil = preProcessorUtil;
    this.protocolProviders = protocolProviders;
    this.workdirProvider = workdirProvider;

    eventBus.register(new CacheClearHook(cacheManager));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Creates a new RepositoryService for the given repository.
   *
   *
   * @param repositoryId id of the repository
   *
   * @return a implementation of RepositoryService
   *         for the given type of repository
   *
   * @throws NotFoundException if no repository
   *         with the given id is available
   * @throws RepositoryServiceNotFoundException if no repository service
   *         implementation for this kind of repository is available
   * @throws IllegalArgumentException if the repository id is null or empty
   * @throws ScmSecurityException if current user has not read permissions
   *         for that repository
   */
  public RepositoryService create(String repositoryId) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(repositoryId),
      "a non empty repositoryId is required");

    Repository repository = repositoryManager.get(repositoryId);

    if (repository == null)
    {
      throw new NotFoundException(Repository.class, repositoryId);
    }

    return create(repository);
  }

  /**
   * Creates a new RepositoryService for the given repository.
   *
   *
   * @param namespaceAndName namespace and name of the repository
   *
   * @return a implementation of RepositoryService
   *         for the given type of repository
   *
   * @throws NotFoundException if no repository
   *         with the given id is available
   * @throws RepositoryServiceNotFoundException if no repository service
   *         implementation for this kind of repository is available
   * @throws IllegalArgumentException if one of the parameters is null or empty
   * @throws ScmSecurityException if current user has not read permissions
   *         for that repository
   */
  public RepositoryService create(NamespaceAndName namespaceAndName)
  {
    Preconditions.checkArgument(namespaceAndName != null,
      "a non empty namespace and name is required");

    Repository repository = repositoryManager.get(namespaceAndName);

    if (repository == null)
    {
      throw notFound(entity(namespaceAndName));
    }

    return create(repository);
  }

  /**
   * Creates a new RepositoryService for the given repository.
   *
   *
   * @param repository the repository
   *
   * @return a implementation of RepositoryService
   *         for the given type of repository
   *
   * @throws RepositoryServiceNotFoundException if no repository service
   *         implementation for this kind of repository is available
   * @throws NullPointerException if the repository is null
   * @throws ScmSecurityException if current user has not read permissions
   *         for that repository
   */
  public RepositoryService create(Repository repository)
  {
    Preconditions.checkNotNull(repository, "repository is required");

    // check for read permissions of current user
    RepositoryPermissions.read(repository).check();

    RepositoryService service = null;

    for (RepositoryServiceResolver resolver : resolvers)
    {
      RepositoryServiceProvider provider = resolver.resolve(repository);

      if (provider != null)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug(
            "create new repository service for repository {} of type {}",
            repository.getName(), repository.getType());
        }

        service = new RepositoryService(cacheManager, provider, repository,
          preProcessorUtil, protocolProviders, workdirProvider);

        break;
      }
    }

    if (service == null)
    {
      throw new RepositoryServiceNotFoundException(repository);
    }

    return service;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Hook and listener to clear all relevant repository caches.
   */
  private static class CacheClearHook
  {

    private final Set<Cache<?, ?>> caches = Sets.newHashSet();

    /**
     * Constructs a new instance and collect all repository relevant
     * caches from the {@link CacheManager}.
     *
     * @param cacheManager cache manager
     */
    public CacheClearHook(CacheManager cacheManager)
    {
      this.caches.add(cacheManager.getCache(BlameCommandBuilder.CACHE_NAME));
      this.caches.add(cacheManager.getCache(BrowseCommandBuilder.CACHE_NAME));
      this.caches.add(cacheManager.getCache(LogCommandBuilder.CACHE_NAME));
      this.caches.add(cacheManager.getCache(TagsCommandBuilder.CACHE_NAME));
      this.caches.add(cacheManager.getCache(BranchesCommandBuilder.CACHE_NAME));
    }

    //~--- methods ------------------------------------------------------------

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
     *
     * @param event hook event
     */
    @Subscribe(referenceType = ReferenceType.STRONG)
    public void onEvent(PostReceiveRepositoryHookEvent event)
    {
      Repository repository = event.getRepository();

      if (repository != null)
      {
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
    public void onEvent(RepositoryEvent event)
    {
      if (event.getEventType() == HandlerEventType.DELETE)
      {
        clearCaches(event.getItem().getId());
      }
    }

    @SuppressWarnings("unchecked")
    private void clearCaches(final String repositoryId)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("clear caches for repository id {}", repositoryId);
      }

      RepositoryCacheKeyPredicate predicate = new RepositoryCacheKeyPredicate(repositoryId);
      caches.forEach((cache) -> {
        cache.removeAll(predicate);
      });
    }
  }


  //~--- fields ---------------------------------------------------------------

  /** cache manager */
  private final CacheManager cacheManager;

  /** scm-manager configuration */
  private final ScmConfiguration configuration;

  /** pre processor util */
  private final PreProcessorUtil preProcessorUtil;

  /** repository manager */
  private final RepositoryManager repositoryManager;

  /** service resolvers */
  private final Set<RepositoryServiceResolver> resolvers;

  private Set<ScmProtocolProvider> protocolProviders;

  private final WorkdirProvider workdirProvider;
}
