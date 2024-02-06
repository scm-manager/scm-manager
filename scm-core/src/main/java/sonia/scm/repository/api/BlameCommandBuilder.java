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
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.BlameResult;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKey;
import sonia.scm.repository.spi.BlameCommand;
import sonia.scm.repository.spi.BlameCommandRequest;

import java.io.IOException;
import java.io.Serializable;

/**
 * Shows changeset information by line for a given file.
 * Blame is also known as annotate in some SCM systems.<br />
 * <br />
 * <b>Sample:</b>
 * <br />
 * <br />
 * Print each line number and code of the file scm-core/pom.xml at
 * revision  60c2f2783368:<br />
 * <pre><code>
 * BlameCommandBuilder blame = repositoryService.getBlameCommand();
 * BlameResult result = blame.setRevision("60c2f2783368")
 *                           .getBlameResult("scm-core/pom.xml");
 *
 * for ( BlameLine line : result ){
 *   System.out.println(line.getLineNumber() + ": " + line.getCode());
 * }
 * </code></pre>
 *
 * @since 1.17
 */
public final class BlameCommandBuilder
{

  static final String CACHE_NAME = "sonia.cache.cmd.blame";

 
  private static final Logger logger =
    LoggerFactory.getLogger(BlameCommandBuilder.class);

  private final BlameCommand blameCommand;

  private final Cache<CacheKey, BlameResult> cache;

  private boolean disableCache = false;

  private boolean disablePreProcessors = false;

  private final PreProcessorUtil preProcessorUtil;

  private final Repository repository;

  private final BlameCommandRequest request = new BlameCommandRequest();

  /**
   * Constructs a new {@link BlameCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param cacheManager cache manager
   * @param blameCommand implementation of the {@link BlameCommand}
   * @param repository repository to query
   * @param preProcessorUtil
   */
  BlameCommandBuilder(CacheManager cacheManager, BlameCommand blameCommand,
                      Repository repository, PreProcessorUtil preProcessorUtil)
  {
    this.cache = cacheManager.getCache(CACHE_NAME);
    this.blameCommand = blameCommand;
    this.repository = repository;
    this.preProcessorUtil = preProcessorUtil;
  }


  /**
   * Reset each parameter to its default value.
   *
   * @return {@code this}
   */
  public BlameCommandBuilder reset()
  {
    request.reset();
    this.disableCache = false;
    this.disablePreProcessors = false;

    return this;
  }


  /**
   * Returns changeset information by line for the given file.
   *
   * @param path path of the file
   * @return changeset information by line for the given file
   *
   * @throws IllegalArgumentException if the path is null or empty
   *
   * @throws IOException
   */
  public BlameResult getBlameResult(String path)
          throws IOException
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(path),
                                "path is required");

    BlameCommandRequest requestClone = request.clone();

    requestClone.setPath(path);

    BlameResult result;

    if (disableCache)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("create blame for {} with disabled cache", requestClone);
      }

      result = blameCommand.getBlameResult(requestClone);
    }
    else
    {
      CacheKey key = new CacheKey(repository, requestClone);

      result = cache.get(key);

      if (result == null)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("create blame for {}", requestClone);
        }

        result = blameCommand.getBlameResult(requestClone);

        if (result != null)
        {
          cache.put(key, result);
        }
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("retrive blame from cache for {}", requestClone);
      }
    }

    if (!disablePreProcessors && (result != null))
    {
      preProcessorUtil.prepareForReturn(repository, result);
    }

    return result;
  }


  /**
   * Disables the cache. This means that every {@link BlameResult}
   * is directly retrieved from the {@link Repository}. <b>Note: </b> Disabling
   * the cache cost a lot of performance and could be much more slower.
   *
   *
   * @param disableCache true to disable the cache
   *
   * @return {@code this}
   */
  public BlameCommandBuilder setDisableCache(boolean disableCache)
  {
    this.disableCache = disableCache;

    return this;
  }
  
  /**
   * Disable the execution of pre-processors.
   *
   *
   * @param disablePreProcessors true to disable the pre processors execution
   *
   * @return {@code this}
   */
  public BlameCommandBuilder setDisablePreProcessors(
          boolean disablePreProcessors)
  {
    this.disablePreProcessors = disablePreProcessors;

    return this;
  }

  /**
   * Sets the start revision for the blame.
   *
   *
   * @param revision revision to start from
   *
   * @return {@code this}
   */
  public BlameCommandBuilder setRevision(String revision)
  {
    request.setRevision(revision);

    return this;
  }

  static class CacheKey implements RepositoryCacheKey, Serializable
  {

      private static final long serialVersionUID = 8373766042131887789L;

    private final String repositoryId;

    private final BlameCommandRequest request;

    public CacheKey(Repository repository, BlameCommandRequest request)
    {
      this.repositoryId = repository.getId();
      this.request = request;
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
             && Objects.equal(request, other.request);
    }

  
    @Override
    public int hashCode()
    {
      return Objects.hashCode(repositoryId, request);
    }

    @Override
    public String getRepositoryId()
    {
      return repositoryId;
    }

  }

}
