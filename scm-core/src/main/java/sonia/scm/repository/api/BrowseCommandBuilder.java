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

import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.BrowseCommand;
import sonia.scm.repository.spi.BrowseCommandRequest;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

/**
 * BrowseCommandBuilder is able to browse the files of a {@link Repository}.
 *
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public final class BrowseCommandBuilder
{

  /** Name of the cache */
  private static final String CACHE_NAME = "sonia.scm.cache.browse";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link LogCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param cacheManager cache manager
   * @param logCommand implementation of the {@link LogCommand}
   * @param browseCommand
   * @param repository repository to query
   */
  BrowseCommandBuilder(CacheManager cacheManager, BrowseCommand browseCommand,
                       Repository repository)
  {
    this.cache = cacheManager.getCache(CacheKey.class, BrowserResult.class,
                                       CACHE_NAME);
    this.browseCommand = browseCommand;
    this.repository = repository;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Reset each parameter to its default value.
   *
   *
   * @return {@code this}
   */
  public BrowseCommandBuilder reset()
  {
    request.reset();

    return this;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Return the files for the given parameters.
   *
   *
   * @return files for the given parameters
   */
  public BrowserResult getBrowserResult()
  {
    BrowserResult result = null;

    if (disableCache)
    {
      result = browseCommand.getBrowserResult(request);
    }
    else
    {
      CacheKey key = new CacheKey(repository, request);

      result = cache.get(key);

      if (result != null)
      {
        cache.put(key, result);
      }
    }

    return result;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Disables the cache. This means that every {@link BrowserResult}
   * is directly retrieved from the {@link Repository}. <b>Note: </b> Disabling
   * the cache cost a lot of performance and could be much more slower.
   *
   *
   * @param disableCache true to disable the cache
   *
   * @return {@code this}
   */
  public BrowseCommandBuilder setDisableCache(boolean disableCache)
  {
    this.disableCache = disableCache;

    return this;
  }

  /**
   * Retrieve only files which are children of the given path.
   * This path have to be a directory.
   *
   * @param path path of the folder
   *
   * @return {@code this}
   */
  public BrowseCommandBuilder setPath(String path)
  {
    request.setPath(path);

    return this;
  }

  /**
   * Retrieve only files of the given revision.
   *
   * @param revision revision for the files
   *
   * @return {@code this}
   */
  public BrowseCommandBuilder setRevision(String revision)
  {
    request.setRevision(revision);

    return this;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Key for cache.
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
    public CacheKey(Repository repository, BrowseCommandRequest request)
    {
      this.repositoryId = repository.getId();
      this.request = request;
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
      return Objects.hashCode(repositoryId, request);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String repositoryId;

    /** Field description */
    private BrowseCommandRequest request;
  }


  //~--- fields ---------------------------------------------------------------

  /** implementation of the browse command */
  private BrowseCommand browseCommand;

  /** cache */
  private Cache<CacheKey, BrowserResult> cache;

  /** disables the cache */
  private boolean disableCache;

  /** the repsitory */
  private Repository repository;

  /** request for the command */
  private BrowseCommandRequest request = new BrowseCommandRequest();
}
