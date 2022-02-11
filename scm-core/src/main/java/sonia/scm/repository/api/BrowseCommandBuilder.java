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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKey;
import sonia.scm.repository.spi.BrowseCommand;
import sonia.scm.repository.spi.BrowseCommandRequest;

import java.io.IOException;
import java.io.Serializable;

//~--- JDK imports ------------------------------------------------------------

/**
 * BrowseCommandBuilder is able to browse the files of a {@link Repository}.
 * <br /><br />
 * <b>Sample:</b>
 * <br />
 * <br />
 * Print all paths from folder scm-core at revision 11aeec7db845:<br />
 * <pre><code>
 * BrowseCommandBuilder browse = repositoryService.getBrowseCommand();
 * BrowserResult result = browse.setPath("scm-core")
 *                              .setRevision("11aeec7db845")
 *                              .getBrowserResult();
 *
 * for ( FileObject fo : result ){
 *   System.out.println( fo.getPath() );
 * }
 * </pre></code>
 *
 * @author Sebastian Sdorra
 * @since 1.17
 */
public final class BrowseCommandBuilder
{

  /** Name of the cache */
  static final String CACHE_NAME = "sonia.cache.cmd.browse";

  /**
   * the logger for BrowseCommandBuilder
   */
  private static final Logger logger =
    LoggerFactory.getLogger(BrowseCommandBuilder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new {@link LogCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param cacheManager cache manager
   * @param browseCommand implementation of the {@link BrowseCommand}
   * @param browseCommand
   * @param repository repository to query
   * @param preProcessorUtil
   */
  BrowseCommandBuilder(CacheManager cacheManager, BrowseCommand browseCommand,
    Repository repository, PreProcessorUtil preProcessorUtil)
  {
    this.cache = cacheManager.getCache(CACHE_NAME);
    this.browseCommand = browseCommand;
    this.repository = repository;
    this.preProcessorUtil = preProcessorUtil;
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
    this.disableCache = false;
    this.disablePreProcessors = false;

    return this;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Return the files for the given parameters.
   *
   *
   * @return files for the given parameters
   *
   * @throws IOException
   */
  public BrowserResult getBrowserResult() throws IOException {
    BrowserResult result = null;

    if (disableCache)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("create browser result for {} with disabled cache",
          request);
      }

      result = browseCommand.getBrowserResult(request);
      if (!request.isRecursive() && request.isCollapsed()) {
        new BrowserResultCollapser().collapseFolders(browseCommand, request, result.getFile());
      }
    }
    else
    {
      CacheKey key = new CacheKey(repository, request);

      result = cache.get(key);

      if (result == null)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("create browser result for {}", request);
        }

        result = browseCommand.getBrowserResult(request);
        if (!request.isRecursive() && request.isCollapsed()) {
          new BrowserResultCollapser().collapseFolders(browseCommand, request, result.getFile());
        }

        if (result != null)
        {
          cache.put(key, result);
        }
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("retrieve browser result from cache for {}", request);
      }
    }

    if (!disablePreProcessors && (result != null))
    {
      preProcessorUtil.prepareForReturn(repository, result);
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
   * Disabling the last commit means that every call to
   * {@link FileObject#getDescription()} and
   * {@link FileObject#getLastModified()} will return {@code null}, but this
   * will also reduce the execution time.
   *
   *
   * @param disableLastCommit true to disable the last commit message
   *
   * @return {@code this}
   *
   * @since 1.26
   */
  public BrowseCommandBuilder setDisableLastCommit(boolean disableLastCommit)
  {
    this.request.setDisableLastCommit(disableLastCommit);

    return this;
  }

  /**
   * Disable the execution of pre processors.
   *
   *
   * @param disablePreProcessors true to disable the pre processors execution
   *
   * @return {@code this}
   */
  public BrowseCommandBuilder setDisablePreProcessors(
    boolean disablePreProcessors)
  {
    this.disablePreProcessors = disablePreProcessors;

    return this;
  }

  /**
   * Enable or disable the detection of sub repositories.
   *
   *
   * @param disableSubRepositoryDetection true to disable sub repository detection.
   *
   * @return {@code this}
   *
   * @since 1.26
   */
  public BrowseCommandBuilder setDisableSubRepositoryDetection(
    boolean disableSubRepositoryDetection)
  {
    this.request.setDisableSubRepositoryDetection(
      disableSubRepositoryDetection);

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
   * Enable or disable recursive file object browsing. Default is disabled.
   *
   * @param recursive true to enable recursive browsing
   *
   * @return {@code this}
   *
   * @since 1.26
   */
  public BrowseCommandBuilder setRecursive(boolean recursive)
  {
    this.request.setRecursive(recursive);

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

  /**
   * Limit the number of result files to <code>limit</code> entries. By default this is set to
   * {@value BrowseCommandRequest#DEFAULT_REQUEST_LIMIT}. Be aware that this parameter can have
   * severe performance implications. Reading a repository with thousands of files in one folder
   * can generate a huge load for a longer time.
   *
   * @param limit The maximal number of files this request shall return (directories are <b>not</b> counted).
   *
   * @since 2.0.0
   */
  public BrowseCommandBuilder setLimit(int limit) {
    request.setLimit(limit);
    return this;
  }

  /**
   * Proceed the list from the given number on (zero based).
   *
   * @param offset The number of the file, the result should start with (zero based).
   *               All preceding files will be omitted. Directories are <b>not</b>
   *               counted. Therefore directories are only listed in results without
   *               offset.
   * @since 2.0.0
   */
  public BrowseCommandBuilder setOffset(int offset) {
    request.setOffset(offset);
    return this;
  }

  /**
   * Collapse empty folders until a folder has content and return the path to such folder as a single item.
   *
   * @param collapse {@code true} if empty folders should be collapsed, otherwise {@code false}.
   * @since 2.30.3
   */
  public BrowseCommandBuilder setCollapse(boolean collapse) {
    request.setCollapse(collapse);
    return this;
  }

  private void updateCache(BrowserResult updatedResult) {
    if (!disableCache) {
      CacheKey key = new CacheKey(repository, request);
      cache.put(key, updatedResult);
    }
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Key for cache.
   *
   *
   * @version        Enter version here..., 12/06/05
   * @author         Enter your name here...
   */
  static class CacheKey implements RepositoryCacheKey, Serializable
  {

    /** Field description */
    private static final long serialVersionUID = 8078650026812373524L;

    //~--- constructors -------------------------------------------------------

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
      this.request = request.clone();
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

    /** request object */
    private final BrowseCommandRequest request;
  }


  //~--- fields ---------------------------------------------------------------

  /** implementation of the browse command */
  private final BrowseCommand browseCommand;

  /** cache */
  private final Cache<CacheKey, BrowserResult> cache;

  /** disables the cache */
  private boolean disableCache = false;

  /** disables the execution of pre processors */
  private boolean disablePreProcessors = false;

  /** Field description */
  private final PreProcessorUtil preProcessorUtil;

  /** the repsitory */
  private final Repository repository;

  /** request for the command */
  private final BrowseCommandRequest request = new BrowseCommandRequest(this::updateCache);
}
