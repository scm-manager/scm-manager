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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Branches;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKey;
import sonia.scm.repository.spi.BranchesCommand;

import java.io.IOException;


/**
 * The branches command list all repository branches.<br />
 * <br />
 * <b>Samples:</b>
 * <br />
 * <br />
 * Return all branches of a repository:<br />
 * <pre><code>
 * BranchesCommandBuilder branchesCommand = repositoryService.getBranchesCommand();
 * Branches branches = tagsCommand.getBranches();
 * </code></pre>
 * @author Sebastian Sdorra
 * @since 1.18
 */
public final class BranchesCommandBuilder
{

  /** name of the cache */
  static final String CACHE_NAME = "sonia.cache.cmd.branches";

  /**
   * the logger for BranchesCommandBuilder
   */
  private static final Logger logger =
    LoggerFactory.getLogger(BranchesCommandBuilder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link BlameCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param cacheManager cache manager
   * @param branchesCommand implementation of the {@link BranchesCommand}
   * @param repository repository to query
   */
  BranchesCommandBuilder(CacheManager cacheManager,
    BranchesCommand branchesCommand, Repository repository)
  {
    this.cache = cacheManager.getCache(CACHE_NAME);
    this.branchesCommand = branchesCommand;
    this.repository = repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns all branches from the repository.
   *
   *
   * @return branches from the repository
   *
   * @throws IOException
   */
  public Branches getBranches() throws IOException
  {
    Branches branches;

    if (disableCache)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("get branches for repository {} with disabled cache",
          repository.getName());
      }

      branches = getBranchesFromCommand();
    }
    else
    {
      CacheKey key = new CacheKey(repository);

      branches = cache.get(key);

      if (branches == null)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("get branches for repository {}", repository);
        }

        branches = getBranchesFromCommand();

        cache.put(key, branches);
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("get branches for repository {} from cache",
          repository.getName());
      }
    }

    return branches;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Disables the cache for tags. This means that every {@link Branch}
   * is directly retrieved from the {@link Repository}. <b>Note: </b> Disabling
   * the cache cost a lot of performance and could be much more slower.
   *
   *
   * @param disableCache true to disable the cache
   *
   * @return {@code this}
   */
  public BranchesCommandBuilder setDisableCache(boolean disableCache)
  {
    this.disableCache = disableCache;

    return this;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  private Branches getBranchesFromCommand()
    throws IOException
  {
    return new Branches(branchesCommand.getBranchesWithStaleFlags(new BranchXDaysOlderThanDefaultStaleComputer()));
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Key for caching branches;
   *
   *
   * @version        Enter version here..., 12/07/05
   * @author         Enter your name here...
   */
  static class CacheKey implements RepositoryCacheKey
  {

    /**
     * Constructs ...
     *
     *
     * @param repository
     */
    public CacheKey(Repository repository)
    {
      this.repositoryId = repository.getId();
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

      return Objects.equal(repositoryId, other.repositoryId);
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
      return Objects.hashCode(repositoryId);
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String getRepositoryId()
    {
      return repositoryId;
    }

    //~--- fields -------------------------------------------------------------

    /** repository id */
    private final String repositoryId;
  }


  //~--- fields ---------------------------------------------------------------

  /** branches command implementation */
  private final BranchesCommand branchesCommand;

  /** cache for branches */
  private final Cache<CacheKey, Branches> cache;

  /** disable cache */
  private boolean disableCache = false;

  /** repository */
  private final Repository repository;
}
