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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.spi.LogCommand;
import sonia.scm.repository.spi.LogCommandRequest;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.Serializable;

/**
 * LogCommandBuilder is able to show the history of a file in a
 * {@link Repository} or the entire history of a {@link Repository}.
 * This class could be used to retrieve a single {@link Changeset} by its id
 * or to get a list of changesets in a {@link ChangesetPagingResult}
 * which can be used for paging.<br />
 * <br />
 * <b>Samples</b>:
 * <br />
 * <br />
 * <b>Get a instance of LogCommandBuilder:</b>
 * <pre><code>
 * public class Sample {
 *
 *   {@literal @}Inject
 *   public Sample(RepositoryServiceFactory factory){
 *     LogCommandBuilder log = factory.create("repository-id").getLogCommand();
 *   }
 *
 * }
 * </code></pre>
 *
 * <b>Retrieve a single {@link Changeset}:</b>
 * <pre><code>
 * LogCommand log = repositoryService.getLogCommand();
 * Changeset changeset = log.getChangeset("id-of-the-commit");
 * </code></pre>
 *
 * <b>Retrieve changesets of a {@link Repository} with paging:</b>
 * <pre><code>
 * LogCommand log = repositoryService.getLogCommand();
 * ChangesetPagingResult changesetPagingResult =
 *          log.setPagingStart(25).setPagingLimit(25).getChangesets();
 * </code></pre>
 *
 * <b>Retrieve all changesets of a file in a {@link Repository}:</b>
 * <pre><code>
 * LogCommand log = repositoryService.getLogCommand();
 * ChangesetPagingResult changesetPagingResult =
 *          log.setPath("pom.xml").disablePagingLimit().getChangesets();
 * </code></pre>
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public final class LogCommandBuilder
{

  /** name of the cache */
  private static final String CACHE_NAME = "sonia.scm.cache.log";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link LogCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param cacheManager cache manager
   * @param logCommand implementation of the {@link LogCommand}
   * @param repository repository to query
   * @param preProcessorUtil
   */
  LogCommandBuilder(CacheManager cacheManager, LogCommand logCommand,
                    Repository repository, PreProcessorUtil preProcessorUtil)
  {
    this.cache = cacheManager.getCache(CacheKey.class,
                                       ChangesetPagingResult.class, CACHE_NAME);
    this.logCommand = logCommand;
    this.repository = repository;
    this.preProcessorUtil = preProcessorUtil;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Disable paging limit all available changesets will be retrieved.
   * This method does the same as {@link #setPagingLimit(int)}
   * with a value of -1.
   *
   *
   * @return {@code this}
   */
  public LogCommandBuilder disablePagingLimit()
  {
    request.setPagingLimit(-1);

    return this;
  }

  /**
   * Reset each parameter to its default value.
   *
   *
   * @return {@code this}
   */
  public LogCommandBuilder reset()
  {
    request.reset();

    return this;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the {@link Changeset} with the given id or null if the
   * {@link Changeset} could not be found in the {@link Repository}.
   *
   *
   * @param id id of the {@link Changeset}
   *
   * @return the {@link Changeset} with the given id or null
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public Changeset getChangeset(String id)
          throws IOException, RepositoryException
  {
    Changeset changeset = null;

    if (disableCache)
    {
      changeset = logCommand.getChangeset(id);
    }
    else
    {
      CacheKey key = new CacheKey(repository, id);
      ChangesetPagingResult cpr = cache.get(key);

      if (cpr == null)
      {
        changeset = logCommand.getChangeset(id);

        if (changeset != null)
        {
          cpr = new ChangesetPagingResult(1, ImmutableList.of(changeset));
          cache.put(key, cpr);
        }
      }
      else
      {
        changeset = cpr.iterator().next();
      }
    }

    if (changeset != null)
    {
      preProcessorUtil.prepareForReturn(repository, changeset);
    }

    return changeset;
  }

  /**
   * Return all changesets with the given parameters.
   *
   *
   * @return all changesets with the given parameters
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public ChangesetPagingResult getChangesets()
          throws IOException, RepositoryException
  {
    ChangesetPagingResult cpr = null;

    if (disableCache)
    {
      cpr = logCommand.getChangesets(request);
    }
    else
    {
      CacheKey key = new CacheKey(repository, request);

      cpr = cache.get(key);

      if (cpr == null)
      {
        cpr = logCommand.getChangesets(request);

        if (cpr != null)
        {
          cache.put(key, cpr);
        }
      }
    }

    if (cpr != null)
    {
      preProcessorUtil.prepareForReturn(repository, cpr);
    }

    return cpr;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Retrieve only changesets of the given branch.
   *
   *
   * @param branch branch name to query
   *
   * @return {@code this}
   */
  public LogCommandBuilder setBranch(String branch)
  {
    request.setBranch(branch);

    return this;
  }

  /**
   * Disables the cache for changesets. This means that every {@link Changeset}
   * is directly retrieved from the {@link Repository}. <b>Note: </b> Disabling
   * the cache cost a lot of performance and could be much more slower.
   *
   *
   * @param disableCache true to disable the cache
   *
   * @return {@code this}
   */
  public LogCommandBuilder setDisableCache(boolean disableCache)
  {
    this.disableCache = disableCache;

    return this;
  }

  /**
   * Retrieve changesets until the given the {@link Changeset}
   * with the given id.
   *
   *
   * @param endChangeset id of the end {@link Changeset}
   *
   * @return {@code this}
   */
  public LogCommandBuilder setEndChangeset(String endChangeset)
  {
    request.setEndChangeset(endChangeset);

    return this;
  }

  /**
   * Set the limit for the returned changesets. The default value is 20.
   * Setting the value to -1 means to disable the limit.
   *
   *
   * @param pagingLimit limit for returned changesets
   *
   * @return {@code this}
   */
  public LogCommandBuilder setPagingLimit(int pagingLimit)
  {
    request.setPagingLimit(pagingLimit);

    return this;
  }

  /**
   * Sets the start value for paging. The value is 0.
   *
   *
   * @param pagingStart start value for paging
   *
   * @return {@code this}
   */
  public LogCommandBuilder setPagingStart(int pagingStart)
  {
    request.setPagingStart(pagingStart);

    return this;
  }

  /**
   * Retrieve only changesets which are affect the given path.
   *
   *
   * @param path file path in the {@link Repository}.
   *
   * @return {@code this}
   */
  public LogCommandBuilder setPath(String path)
  {
    request.setPath(path);

    return this;
  }

  /**
   * Start at the given {@link Changeset}.
   *
   *
   * @param startChangeset changeset id to start with
   *
   * @return {@code this}
   */
  public LogCommandBuilder setStartChangeset(String startChangeset)
  {
    request.setStartChangeset(startChangeset);

    return this;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/06/05
   * @author         Enter your name here...
   */
  private static class CacheKey implements Serializable
  {

    /**
     * Constructs ...
     *
     *
     * @param repository
     * @param request
     */
    public CacheKey(Repository repository, LogCommandRequest request)
    {
      this.repositoryId = repository.getId();
      this.request = request;
    }

    /**
     * Constructs ...
     *
     *
     *
     * @param repository
     * @param changesetId
     */
    public CacheKey(Repository repository, String changesetId)
    {
      this.repositoryId = repository.getId();
      this.changesetId = changesetId;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param obj
     *
     * @return
     */
    @Override
    public boolean equals(Object obj)
    {
      if (obj == null)
      {
        return false;
      }

      if (getClass() != obj.getClass())
      {
        return false;
      }

      final CacheKey other = (CacheKey) obj;

      return Objects.equal(repositoryId, other.repositoryId)
             && Objects.equal(changesetId, other.changesetId)
             && Objects.equal(request, other.request);
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int hashCode()
    {
      return Objects.hashCode(repositoryId, changesetId, request);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String changesetId;

    /** Field description */
    private String repositoryId;

    /** Field description */
    private LogCommandRequest request;
  }


  //~--- fields ---------------------------------------------------------------

  /** cache for changesets */
  private Cache<CacheKey, ChangesetPagingResult> cache;

  /** disable cache */
  private boolean disableCache;

  /** Implementation of the log command */
  private LogCommand logCommand;

  /** Field description */
  private PreProcessorUtil preProcessorUtil;

  /** repository to query */
  private Repository repository;

  /** request for the log command */
  private LogCommandRequest request = new LogCommandRequest();
}
