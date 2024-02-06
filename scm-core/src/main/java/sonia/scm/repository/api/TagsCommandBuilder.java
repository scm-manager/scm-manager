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
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKey;
import sonia.scm.repository.Tag;
import sonia.scm.repository.Tags;
import sonia.scm.repository.spi.TagsCommand;

import java.io.IOException;
import java.util.List;

/**
 * The tags command list all repository tags.<br />
 * <br />
 * <b>Samples:</b>
 * <br />
 * <br />
 * Return all tags of a repository:<br />
 * <pre><code>
 * TagsCommandBuilder tagsCommand = repositoryService.getTagsCommand();
 * Tags tags = tagsCommand.getTags();
 * </code></pre>
 * @since 1.18
 */
public final class TagsCommandBuilder
{

  static final String CACHE_NAME = "sonia.cache.cmd.tags";

 
  private static final Logger logger =
    LoggerFactory.getLogger(TagsCommandBuilder.class);

  /** cache for changesets */
  private final Cache<CacheKey, Tags> cache;

  private final TagsCommand command;

  private final Repository repository;

  private boolean disableCache = false;

  /**
   * Constructs a new {@link TagsCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param cacheManager cache manager
   * @param command implementation of the {@link TagsCommand}
   * @param repository repository
   */
  TagsCommandBuilder(CacheManager cacheManager, TagsCommand command,
    Repository repository)
  {
    this.cache = cacheManager.getCache(CACHE_NAME);
    this.command = command;
    this.repository = repository;
  }


  /**
   * Returns all tags from the repository.
   */
  public Tags getTags() throws IOException {
    Tags tags;

    if (disableCache)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("get tags for repository {} with disabled cache",
          repository.getName());
      }

      tags = getTagsFromCommand();
    }
    else
    {
      CacheKey key = new CacheKey(repository);

      tags = cache.get(key);

      if (tags == null)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("get tags for repository {}", repository);
        }

        tags = getTagsFromCommand();

        if (tags != null)
        {
          cache.put(key, tags);
        }
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("get tags for repository {} from cache",
          repository.getName());
      }
    }

    return tags;
  }


  /**
   * Disables the cache for tags. This means that every {@link Tag}
   * is directly retrieved from the {@link Repository}. <b>Note: </b> Disabling
   * the cache cost a lot of performance and could be much slower.
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



  private Tags getTagsFromCommand() throws IOException
  {
    List<Tag> tagList = command.getTags();

    return new Tags(tagList);
  }




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
