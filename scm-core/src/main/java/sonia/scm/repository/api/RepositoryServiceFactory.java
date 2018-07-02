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



package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.ReferenceType;
import com.github.legman.Subscribe;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.HandlerEventType;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKeyPredicate;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.spi.RepositoryServiceProvider;
import sonia.scm.repository.spi.RepositoryServiceResolver;
import sonia.scm.security.ScmSecurityException;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.ClearRepositoryCacheEvent;

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
   *
   * @since 1.21
   */
  @Inject
  public RepositoryServiceFactory(ScmConfiguration configuration,
    CacheManager cacheManager, RepositoryManager repositoryManager,
    Set<RepositoryServiceResolver> resolvers, PreProcessorUtil preProcessorUtil)
  {
    this.configuration = configuration;
    this.cacheManager = cacheManager;
    this.repositoryManager = repositoryManager;
    this.resolvers = resolvers;
    this.preProcessorUtil = preProcessorUtil;

    ScmEventBus.getInstance().register(new CacheClearHook(cacheManager));
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
   * @throws RepositoryNotFoundException if no repository
   *         with the given id is available
   * @throws RepositoryServiceNotFoundException if no repository service
   *         implementation for this kind of repository is available
   * @throws IllegalArgumentException if the repository id is null or empty
   * @throws ScmSecurityException if current user has not read permissions
   *         for that repository
   */
  public RepositoryService create(String repositoryId)
    throws RepositoryNotFoundException
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(repositoryId),
      "a non empty repositoryId is required");

    Repository repository = repositoryManager.get(repositoryId);

    if (repository == null)
    {
      throw new RepositoryNotFoundException(
        "could not find a repository with id ".concat(repositoryId));
    }

    return create(repository);
  }

  /**
   * Creates a new RepositoryService for the given repository.
   *
   *
   * @param type type of the repository
   * @param name name of the repository
   *
   * @return a implementation of RepositoryService
   *         for the given type of repository
   *
   * @throws RepositoryNotFoundException if no repository
   *         with the given id is available
   * @throws RepositoryServiceNotFoundException if no repository service
   *         implementation for this kind of repository is available
   * @throws IllegalArgumentException if one of the parameters is null or empty
   * @throws ScmSecurityException if current user has not read permissions
   *         for that repository
   */
  public RepositoryService create(String type, String name)
    throws RepositoryNotFoundException
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(type),
      "a non empty type is required");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(name),
      "a non empty name is required");

    Repository repository = repositoryManager.get(type, name);

    if (repository == null)
    {
      StringBuilder msg =
        new StringBuilder("could not find a repository with type ");

      msg.append(type).append(" and name ").append(name);

      throw new RepositoryNotFoundException(msg.toString());
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
    RepositoryPermissions.read(repository);

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
          preProcessorUtil);

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
     * @param repository changed repository
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
}
