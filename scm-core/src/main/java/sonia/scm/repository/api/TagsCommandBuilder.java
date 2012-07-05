/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.api;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKey;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.Tag;
import sonia.scm.repository.Tags;
import sonia.scm.repository.spi.TagsCommand;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;

/**
 * The tags command list all repository tag.<br />
 * <br />
 * <b>Samples:</b>
 * <br />
 * <br />
 * Return all tags of a repository:<br />
 * <pre><code>
 * TagsCommandBuilder tagsCommand = repositoryService.getLogCommand();
 * Tags tags = tagsCommand.getTags();
 * </code></pre>
 * @author Sebastian Sdorra
 */
public final class TagsCommandBuilder
{

  /** name of the cache */
  static final String CACHE_NAME = "sonia.cache.cmd.tags";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link TagsCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param cacheManager cache manager
   * @param logCommand implementation of the {@link TagsCommand}
   * @param command
   * @param repository repository
   */
  TagsCommandBuilder(CacheManager cacheManager, TagsCommand command,
    Repository repository)
  {
    this.cache = cacheManager.getCache(CacheKey.class, Tags.class, CACHE_NAME);
    this.command = command;
    this.repository = repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns all tags from the repository.
   *
   *
   * @return tags from the repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public Tags getTags() throws RepositoryException, IOException
  {
    Tags tags = null;

    if (disableCache)
    {
      tags = getTagsFromCommand();
    }
    else
    {
      CacheKey key = new CacheKey(repository);

      tags = cache.get(key);

      if (tags == null)
      {
        tags = getTagsFromCommand();

        if (tags != null)
        {
          cache.put(key, tags);
        }
      }
    }

    return tags;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Disables the cache for tags. This means that every {@link Tag}
   * is directly retrieved from the {@link Repository}. <b>Note: </b> Disabling
   * the cache cost a lot of performance and could be much more slower.
   *
   *
   * @param disableCache true to disable the cache
   *
   * @return {@code this}
   */
  public TagsCommandBuilder setDisableCache(boolean disableCache)
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
   * @throws RepositoryException
   */
  private Tags getTagsFromCommand() throws RepositoryException, IOException
  {
    List<Tag> tagList = command.getTags();

    return new Tags(tagList);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
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

    /** Field description */
    private String repositoryId;
  }


  //~--- fields ---------------------------------------------------------------

  /** cache for changesets */
  private Cache<CacheKey, Tags> cache;

  /** Field description */
  private TagsCommand command;

  /** Field description */
  private boolean disableCache = false;

  /** Field description */
  private Repository repository;
}
