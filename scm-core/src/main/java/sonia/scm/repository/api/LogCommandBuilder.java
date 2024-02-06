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


import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.FeatureNotSupportedException;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Feature;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKey;
import sonia.scm.repository.spi.LogCommand;
import sonia.scm.repository.spi.LogCommandRequest;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

/**
 * LogCommandBuilder is able to show the history of a file in a
 * {@link Repository} or the entire history of a {@link Repository}.
 * This class could be used to retrieve a single {@link Changeset} by its id
 * or to get a list of changesets in a {@link ChangesetPagingResult}
 * which can be used for paging.<br />
 * <br />
 * <b>Samples:</b>
 * <br />
 * <br />
 * Retrieve a single {@link Changeset}:<br />
 * <pre><code>
 * LogCommandBuilder log = repositoryService.getLogCommand();
 * Changeset changeset = log.getChangeset("id-of-the-commit");
 * </code></pre>
 *
 * Retrieve changesets of a {@link Repository} with paging:<br />
 * <pre><code>
 * LogCommandBuilder log = repositoryService.getLogCommand();
 * ChangesetPagingResult changesetPagingResult =
 *          log.setPagingStart(25).setPagingLimit(25).getChangesets();
 * </code></pre>
 *
 * Retrieve all changesets of a file in a {@link Repository}:<br />
 * <pre><code>
 * LogCommandBuilder log = repositoryService.getLogCommand();
 * ChangesetPagingResult changesetPagingResult =
 *          log.setPath("pom.xml").disablePagingLimit().getChangesets();
 * </code></pre>
 *
 * @since 1.17
 */
public final class LogCommandBuilder
{

  static final String CACHE_NAME = "sonia.cache.cmd.log";

  private static final Logger logger =
    LoggerFactory.getLogger(LogCommandBuilder.class);

  /** cache for changesets */
  private final Cache<CacheKey, ChangesetPagingResult> cache;

  private final LogCommand logCommand;

  private final PreProcessorUtil preProcessorUtil;
  private Set<Feature> supportedFeatures;

  private final Repository repository;

  private boolean disableCache = false;

  private boolean disablePreProcessors = false;

  private final LogCommandRequest request = new LogCommandRequest();

  /**
   * Constructs a new {@link LogCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *  @param cacheManager cache manager
   * @param logCommand implementation of the {@link LogCommand}
   * @param repository repository to query
   * @param preProcessorUtil
   * @param supportedFeatures The supported features of the provider
   */
  LogCommandBuilder(CacheManager cacheManager, LogCommand logCommand,
                    Repository repository, PreProcessorUtil preProcessorUtil, Set<Feature> supportedFeatures)
  {
    this.cache = cacheManager.getCache(CACHE_NAME);
    this.logCommand = logCommand;
    this.repository = repository;
    this.preProcessorUtil = preProcessorUtil;
    this.supportedFeatures = supportedFeatures;
  }


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
    this.disableCache = false;
    this.disablePreProcessors = false;

    return this;
  }


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
   */
  public Changeset getChangeset(String id) throws IOException {
    Changeset changeset;

    if (disableCache)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("get changeset for {} with disabled cache", id);
      }

      changeset = logCommand.getChangeset(id, request);
    }
    else
    {
      CacheKey key = new CacheKey(repository, id);
      ChangesetPagingResult cpr = cache.get(key);

      if (cpr == null)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("get changeset for {}", id);
        }

        changeset = logCommand.getChangeset(id, request);

        if (changeset != null)
        {
          cpr = new ChangesetPagingResult(1, ImmutableList.of(changeset));
          cache.put(key, cpr);
        }
      }
      else
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("get changeset {} from cache", id);
        }

        changeset = cpr.iterator().next();
      }
    }

    if (!disablePreProcessors && (changeset != null))
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
   */
  public ChangesetPagingResult getChangesets() throws IOException {
    ChangesetPagingResult cpr;

    if (disableCache)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("get changesets for {} with disabled cache", request);
      }

      cpr = logCommand.getChangesets(request);
    }
    else
    {
      CacheKey key = new CacheKey(repository, request);

      cpr = cache.get(key);

      if (cpr == null)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("get changesets for {}", request);
        }

        cpr = logCommand.getChangesets(request);

        if (cpr != null)
        {
          cache.put(key, cpr);
        }
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("get changesets from cache for {}", request);
      }
    }

    if (!disablePreProcessors && (cpr != null))
    {
      preProcessorUtil.prepareForReturn(repository, cpr);
    }

    return cpr;
  }


  /**
   * Retrieves only changesets of the given branch.<br />
   * <b>Note:</b> This option is ignored if the underlying
   * {@link RepositoryService} does not support the {@link Command#BRANCHES}.
   *
   *
   * @param branch brnach to retrieve changesets from
   *
   * @return retrieves only changesets of the given branch
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
   * Disable the execution of pre processors if set to <code>true</code>.
   */
  public LogCommandBuilder setDisablePreProcessors(boolean disablePreProcessors)
  {
    this.disablePreProcessors = disablePreProcessors;

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

  /**
   * Compute the incoming changes of the branch set with {@link #setBranch(String)} in respect to the changeset given
   * here. In other words: What changesets would be new to the ancestor changeset given here when the branch would
   * be merged into it. Requires feature {@link sonia.scm.repository.Feature#INCOMING_REVISION}!
   *
   * @return {@code this}
   */
  public LogCommandBuilder setAncestorChangeset(String ancestorChangeset) {
    if (!supportedFeatures.contains(Feature.INCOMING_REVISION)) {
      throw new FeatureNotSupportedException(Feature.INCOMING_REVISION.name());
    }
    request.setAncestorChangeset(ancestorChangeset);
    return this;
  }




  static class CacheKey implements RepositoryCacheKey, Serializable
  {

      private static final long serialVersionUID = 5701675009949268863L;

    private final String changesetId;

    private final String repositoryId;

    private final LogCommandRequest request;


    public CacheKey(Repository repository, LogCommandRequest request)
    {
      this.repositoryId = repository.getId();
      this.request = request;
      this.changesetId = null;
    }

    public CacheKey(Repository repository, String changesetId)
    {
      this.repositoryId = repository.getId();
      this.changesetId = changesetId;
      this.request = null;
    }




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


    @Override
    public int hashCode()
    {
      return Objects.hashCode(repositoryId, changesetId, request);
    }


    @Override
    public String getRepositoryId()
    {
      return repositoryId;
    }

  }
}
