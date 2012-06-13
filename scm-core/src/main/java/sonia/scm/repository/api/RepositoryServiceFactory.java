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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import sonia.scm.cache.CacheManager;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.PermissionUtil;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.spi.RepositoryServiceProvider;
import sonia.scm.repository.spi.RepositoryServiceResolver;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

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
 *     RepositoryService service = factory.create(repositoryId);
 *     return service.getLogCommand().getChangeset(commitId);
 *   }
 *
 * }
 * </code></pre>
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
@Singleton
public final class RepositoryServiceFactory
{

  /**
   * Constructs a new {@link RepositoryServiceFactory}. This constructor
   * should not be called manually, it should only be used by the injection
   * container.
   *
   * @param cacheManager cache manager
   * @param repositoryManager manager for repositories
   * @param securityContextProvider provider for the current security context
   * @param resolvers a set of {@link RepositoryServiceResolver}
   * @param preProcessorUtil helper object for pre processor handling
   */
  @Inject
  public RepositoryServiceFactory(
          CacheManager cacheManager, RepositoryManager repositoryManager,
          Provider<WebSecurityContext> securityContextProvider,
          Set<RepositoryServiceResolver> resolvers,
          PreProcessorUtil preProcessorUtil)
  {
    this.cacheManager = cacheManager;
    this.repositoryManager = repositoryManager;
    this.securityContextProvider = securityContextProvider;
    this.resolvers = resolvers;
    this.preProcessorUtil = preProcessorUtil;
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
    PermissionUtil.assertPermission(repository, securityContextProvider,
                                    PermissionType.READ);

    RepositoryService service = null;

    for (RepositoryServiceResolver resolver : resolvers)
    {
      RepositoryServiceProvider provider = resolver.reslove(repository);

      if (provider != null)
      {
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private CacheManager cacheManager;

  /** Field description */
  private PreProcessorUtil preProcessorUtil;

  /** Field description */
  private RepositoryManager repositoryManager;

  /** Field description */
  private Set<RepositoryServiceResolver> resolvers;

  /** Field description */
  private Provider<WebSecurityContext> securityContextProvider;
}
