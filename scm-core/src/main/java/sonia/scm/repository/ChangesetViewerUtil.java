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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.NotSupportedFeatuerException;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.6
 */
public class ChangesetViewerUtil extends PartCacheClearHook
{

  /** Field description */
  public static final String CACHE_NAME = "sonia.cache.repository.changesets";

  /** the logger for ChangesetViewerUtil */
  private static final Logger logger =
    LoggerFactory.getLogger(ChangesetViewerUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repositoryManager
   * @param cacheManager
   * @param changesetPreProcessorSet
   * @param changesetPreProcessorFactorySet
   */
  @Inject
  public ChangesetViewerUtil(
          RepositoryManager repositoryManager, CacheManager cacheManager,
          Set<ChangesetPreProcessor> changesetPreProcessorSet,
          Set<ChangesetPreProcessorFactory> changesetPreProcessorFactorySet)
  {
    this.repositoryManager = repositoryManager;
    this.changesetPreProcessorSet = changesetPreProcessorSet;
    this.changesetPreProcessorFactorySet = changesetPreProcessorFactorySet;
    cache = cacheManager.getCache(ChangesetViewerCacheKey.class,
                                  ChangesetPagingResult.class, CACHE_NAME);
    init(repositoryManager, cache);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param start
   * @param max
   *
   * @return
   *
   *
   * @throws IOException
   * @throws NotSupportedFeatuerException
   * @throws RepositoryException
   */
  public ChangesetPagingResult getChangesets(String repositoryId, int start,
          int max)
          throws IOException, RepositoryException, NotSupportedFeatuerException
  {
    AssertUtil.assertIsNotEmpty(repositoryId);

    Repository repository = repositoryManager.get(repositoryId);

    if (repository == null)
    {
      throw new RepositoryNotFoundException(
          "could not find repository with id ".concat(repositoryId));
    }

    return getChangesets(repository, start, max);
  }

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param path
   * @param start
   * @param max
   *
   * @return
   *
   *
   * @throws IOException
   * @throws NotSupportedFeatuerException
   * @throws RepositoryException
   */
  public ChangesetPagingResult getChangesets(String repositoryId, String path,
          int start, int max)
          throws IOException, RepositoryException, NotSupportedFeatuerException
  {
    AssertUtil.assertIsNotEmpty(repositoryId);

    Repository repository = repositoryManager.get(repositoryId);

    if (repository == null)
    {
      throw new RepositoryNotFoundException(
          "could not find repository with id ".concat(repositoryId));
    }

    return getChangesets(repository, path, start, max);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param start
   * @param max
   *
   * @return
   *
   *
   * @throws IOException
   * @throws NotSupportedFeatuerException
   * @throws RepositoryException
   */
  public ChangesetPagingResult getChangesets(Repository repository, int start,
          int max)
          throws IOException, RepositoryException, NotSupportedFeatuerException
  {
    AssertUtil.assertIsNotNull(repository);

    ChangesetViewer viewer = repositoryManager.getChangesetViewer(repository);

    if (viewer == null)
    {
      throw new NotSupportedFeatuerException(
          "ChangesetViewer is not supported for type ".concat(
            repository.getType()));
    }

    ChangesetViewerCacheKey key =
      new ChangesetViewerCacheKey(repository.getId(), Util.EMPTY_STRING, start,
                                  max);
    ChangesetPagingResult result = cache.get(key);

    if (result == null)
    {
      result = viewer.getChangesets(start, max);

      if (result != null)
      {
        if (Util.isNotEmpty(result.getChangesets()))
        {
          callPreProcessors(result);
          callPreProcessorFactories(repository, result);
        }

        cache.put(key, result);
      }
      else
      {
        throw new RepositoryException("could not fetch changesets");
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("fetch changesetviewer results from cache");
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param path
   * @param start
   * @param max
   *
   * @return
   *
   *
   * @throws IOException
   * @throws NotSupportedFeatuerException
   * @throws RepositoryException
   */
  public ChangesetPagingResult getChangesets(Repository repository,
          String path, int start, int max)
          throws IOException, RepositoryException, NotSupportedFeatuerException
  {
    AssertUtil.assertIsNotNull(repository);

    ChangesetViewer viewer = repositoryManager.getChangesetViewer(repository);

    if (viewer == null)
    {
      throw new NotSupportedFeatuerException(
          "ChangesetViewer is not supported for type ".concat(
            repository.getType()));
    }

    ChangesetViewerCacheKey key =
      new ChangesetViewerCacheKey(repository.getId(), path, start, max);
    ChangesetPagingResult result = cache.get(key);

    if (result == null)
    {
      result = viewer.getChangesets(path, start, max);

      if (result != null)
      {
        if (Util.isNotEmpty(result.getChangesets()))
        {
          callPreProcessors(result);
          callPreProcessorFactories(repository, result);
        }

        cache.put(key, result);
      }
      else
      {
        throw new RepositoryException("could not fetch changesets");
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("fetch changesetviewer results from cache");
    }

    return result;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param repository
   * @param changesets
   */
  private void callPreProcessorFactories(Repository repository,
          ChangesetPagingResult changesets)
  {
    if (Util.isNotEmpty(changesetPreProcessorFactorySet))
    {
      for (ChangesetPreProcessorFactory factory :
              changesetPreProcessorFactorySet)
      {
        ChangesetPreProcessor cpp = factory.createPreProcessor(repository);

        if (cpp != null)
        {
          for (Changeset c : changesets.getChangesets())
          {
            cpp.process(c);
          }
        }
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param changesets
   */
  private void callPreProcessors(ChangesetPagingResult changesets)
  {
    if (Util.isNotEmpty(changesetPreProcessorSet))
    {
      for (Changeset c : changesets.getChangesets())
      {
        for (ChangesetPreProcessor cpp : changesetPreProcessorSet)
        {
          cpp.process(c);
        }
      }
    }
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 11/07/24
   * @author         Enter your name here...
   */
  private class ChangesetViewerCacheKey implements RepositoryCacheKey
  {

    /**
     * Constructs ...
     *
     *
     * @param repository
     * @param path
     * @param start
     * @param max
     */
    public ChangesetViewerCacheKey(String repository, String path, int start,
                                   int max)
    {
      this.repository = repository;
      this.path = path;
      this.start = start;
      this.max = max;
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

      final ChangesetViewerCacheKey other = (ChangesetViewerCacheKey) obj;

      if ((this.repository == null)
          ? (other.repository != null)
          : !this.repository.equals(other.repository))
      {
        return false;
      }

      if ((this.path == null)
          ? (other.path != null)
          : !this.path.equals(other.path))
      {
        return false;
      }

      if (this.start != other.start)
      {
        return false;
      }

      if (this.max != other.max)
      {
        return false;
      }

      return true;
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
      int hash = 3;

      hash = 67 * hash + ((this.repository != null)
                          ? this.repository.hashCode()
                          : 0);
      hash = 67 * hash + this.start;
      hash = 67 * hash + this.max;

      return hash;
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
      return repository;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private int max;

    /** Field description */
    private String path;

    /** Field description */
    private String repository;

    /** Field description */
    private int start;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Cache<ChangesetViewerCacheKey, ChangesetPagingResult> cache;

  /** Field description */
  private Set<ChangesetPreProcessorFactory> changesetPreProcessorFactorySet;

  /** Field description */
  private Set<ChangesetPreProcessor> changesetPreProcessorSet;

  /** Field description */
  private RepositoryManager repositoryManager;
}
