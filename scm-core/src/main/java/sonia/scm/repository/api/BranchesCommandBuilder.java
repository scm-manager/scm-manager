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
 * @since 1.18
 */
public final class BranchesCommandBuilder
{

  static final String CACHE_NAME = "sonia.cache.cmd.branches";

  /** branches command implementation */
  private final BranchesCommand branchesCommand;

  /** cache for branches */
  private final Cache<CacheKey, Branches> cache;

  private boolean disableCache = false;

  private final Repository repository;
 
  private static final Logger logger =
    LoggerFactory.getLogger(BranchesCommandBuilder.class);


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
        logger.debug("get branches for repository {} with disabled cache", repository);
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
        logger.debug("get branches for repository {} from cache", repository);
      }
    }

    return branches;
  }


  /**
   * Disables the cache for tags. This means that every {@link Branch}
   * is directly retrieved from the {@link Repository}. <b>Note: </b> Disabling
   * the cache cost a lot of performance and could be much slower.
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



  private Branches getBranchesFromCommand()
    throws IOException
  {
    return new Branches(branchesCommand.getBranchesWithStaleFlags(new BranchXDaysOlderThanDefaultStaleComputer()));
  }



  /**
   * Key for caching branches;
   */
  static class CacheKey implements RepositoryCacheKey
  {
    private final String repositoryId;
  
    public CacheKey(Repository repository)
    {
      this.repositoryId = repository.getId();
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

      return Objects.equal(repositoryId, other.repositoryId);
    }

  
    @Override
    public int hashCode()
    {
      return Objects.hashCode(repositoryId);
    }

    
    @Override
    public String getRepositoryId()
    {
      return repositoryId;
    }
  }

}
