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
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.NotSupportedFeatuerException;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.6
 *
 * @deprecated use {@link RepositoryService#getBrowseCommand()} instead.
 */
@Singleton
@Deprecated
public class RepositoryBrowserUtil extends PartCacheClearHook
{

  /** Field description */
  public static final String CACHE_NAME = "sonia.cache.repository.browser";

  /** the logger for RepositoryBrowserUtil */
  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryBrowserUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repositoryManager
   * @param cacheManager
   * @param preProcessorSet
   * @param preProcessorFactorySet
   */
  @Inject
  public RepositoryBrowserUtil(
          RepositoryManager repositoryManager, CacheManager cacheManager,
          Set<FileObjectPreProcessor> preProcessorSet,
          Set<FileObjectPreProcessorFactory> preProcessorFactorySet)
  {
    this.repositoryManager = repositoryManager;
    this.cache = cacheManager.getCache(RepositoryBrowserCacheKey.class,
                                       BrowserResult.class, CACHE_NAME);
    this.preProcessorSet = preProcessorSet;
    this.preProcessorFactorySet = preProcessorFactorySet;
    init(repositoryManager, cache);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param revision
   * @param path
   *
   * @return
   *
   * @throws IOException
   * @throws NotSupportedFeatuerException
   * @throws RepositoryException
   */
  public BrowserResult getResult(String repositoryId, String revision,
                                 String path)
          throws RepositoryException, NotSupportedFeatuerException, IOException
  {
    AssertUtil.assertIsNotEmpty(repositoryId);

    Repository repository = repositoryManager.get(repositoryId);

    if (repository == null)
    {
      throw new RepositoryNotFoundException(
          "could not find repository with id ".concat(repositoryId));
    }

    return getResult(repository, revision, path);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param revision
   * @param path
   *
   * @return
   *
   * @throws IOException
   * @throws NotSupportedFeatuerException
   * @throws RepositoryException
   */
  public BrowserResult getResult(Repository repository, String revision,
                                 String path)
          throws RepositoryException, NotSupportedFeatuerException, IOException
  {
    AssertUtil.assertIsNotNull(repository);

    RepositoryBrowser browser =
      repositoryManager.getRepositoryBrowser(repository);

    if (browser == null)
    {
      throw new NotSupportedFeatuerException(
          "RepositoryBrowser is not supported for type ".concat(
            repository.getType()));
    }

    RepositoryBrowserCacheKey key =
      new RepositoryBrowserCacheKey(repository.getId(), revision, path);
    BrowserResult result = cache.get(key);

    if (result == null)
    {
      result = browser.getResult(revision, path);

      if (result != null)
      {
        sort(result);
        EscapeUtil.escape(result);
        callPreProcessors(result);
        callPreProcessorFactories(repository, result);
      }
      else
      {
        result = new BrowserResult();
      }

      cache.put(key, result);
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("fetch repositorybrowser results from cache");
    }

    return result;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param fopp
   * @param result
   */
  private void callPreProcessor(FileObjectPreProcessor fopp,
                                BrowserResult result)
  {
    if (fopp != null)
    {
      List<FileObject> foList = result.getFiles();

      if (Util.isNotEmpty(foList))
      {
        for (FileObject fo : foList)
        {
          fopp.process(fo);
        }
      }
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param repository
   * @param result
   */
  private void callPreProcessorFactories(Repository repository,
          BrowserResult result)
  {
    if (Util.isNotEmpty(preProcessorFactorySet))
    {
      for (FileObjectPreProcessorFactory factory : preProcessorFactorySet)
      {
        FileObjectPreProcessor fopp = factory.createPreProcessor(repository);

        callPreProcessor(fopp, result);
      }
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param result
   */
  private void callPreProcessors(BrowserResult result)
  {
    if (Util.isNotEmpty(preProcessorSet))
    {
      for (FileObjectPreProcessor fopp : preProcessorSet)
      {
        callPreProcessor(fopp, result);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param result
   */
  private void sort(BrowserResult result)
  {
    List<FileObject> files = result.getFiles();

    if (files != null)
    {
      Collections.sort(files, FileObjectNameComparator.instance);
    }
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 11/08/03
   * @author         Enter your name here...
   */
  private static class RepositoryBrowserCacheKey implements RepositoryCacheKey
  {

    /**
     * Constructs ...
     *
     *
     * @param repositoryId
     * @param revision
     * @param path
     */
    public RepositoryBrowserCacheKey(String repositoryId, String revision,
                                     String path)
    {
      this.repositoryId = repositoryId;
      this.revision = revision;
      this.path = path;
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

      final RepositoryBrowserCacheKey other = (RepositoryBrowserCacheKey) obj;

      if ((this.repositoryId == null)
          ? (other.repositoryId != null)
          : !this.repositoryId.equals(other.repositoryId))
      {
        return false;
      }

      if ((this.revision == null)
          ? (other.revision != null)
          : !this.revision.equals(other.revision))
      {
        return false;
      }

      if ((this.path == null)
          ? (other.path != null)
          : !this.path.equals(other.path))
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

      hash = 53 * hash + ((this.repositoryId != null)
                          ? this.repositoryId.hashCode()
                          : 0);
      hash = 53 * hash + ((this.revision != null)
                          ? this.revision.hashCode()
                          : 0);
      hash = 53 * hash + ((this.path != null)
                          ? this.path.hashCode()
                          : 0);

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
      return repositoryId;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String path;

    /** Field description */
    private String repositoryId;

    /** Field description */
    private String revision;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Cache<RepositoryBrowserCacheKey, BrowserResult> cache;

  /** Field description */
  private Set<FileObjectPreProcessorFactory> preProcessorFactorySet;

  /** Field description */
  private Set<FileObjectPreProcessor> preProcessorSet;

  /** Field description */
  private RepositoryManager repositoryManager;
}
