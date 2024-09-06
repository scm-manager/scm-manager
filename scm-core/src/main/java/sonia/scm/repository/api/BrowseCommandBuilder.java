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
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCacheKey;
import sonia.scm.repository.spi.BrowseCommand;
import sonia.scm.repository.spi.BrowseCommandRequest;

import java.io.IOException;
import java.io.Serializable;
import java.util.function.Supplier;

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
 * @since 1.17
 */
public final class BrowseCommandBuilder
{

  static final String CACHE_NAME = "sonia.cache.cmd.browse";


  private static final Logger logger =
    LoggerFactory.getLogger(BrowseCommandBuilder.class);

  private final BrowseCommand browseCommand;

  private final Cache<CacheKey, BrowserResult> cache;

  private boolean disableCache = false;

  private boolean disablePreProcessors = false;

  private final PreProcessorUtil preProcessorUtil;
  private final Supplier<BrowseCommand> browseCommandFactory;

  private final Repository repository;

  private final BrowseCommandRequest request = new BrowseCommandRequest(this::updateCache);

  /**
   * Constructs a new {@link BrowseCommandBuilder}, this constructor should
   * only be called from the {@link RepositoryService}.
   *
   * @param cacheManager cache manager
   * @param browseCommand implementation of the {@link BrowseCommand}
   * @param repository repository to query
   * @param preProcessorUtil this factory is used to create browse commands for the collapse feature
   */
  BrowseCommandBuilder(CacheManager cacheManager, BrowseCommand browseCommand,
                       Repository repository, PreProcessorUtil preProcessorUtil,
                       Supplier<BrowseCommand> browseCommandFactory)
  {
    this.cache = cacheManager.getCache(CACHE_NAME);
    this.browseCommand = browseCommand;
    this.repository = repository;
    this.preProcessorUtil = preProcessorUtil;
    this.browseCommandFactory = browseCommandFactory;
  }


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


  /**
   * Return the files for the given parameters.
   *
   * @throws IOException
   */
  public BrowserResult getBrowserResult() throws IOException {
    BrowserResult result;

    if (disableCache)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("create browser result for {} with disabled cache",
          request);
      }

      result = computeBrowserResult();
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

        result = computeBrowserResult();

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

  private BrowserResult computeBrowserResult() throws IOException {
    BrowserResult result = browseCommand.getBrowserResult(request);
    if (result != null && !request.isRecursive() && request.isCollapse()) {
      new BrowserResultCollapser().collapseFolders(browseCommandFactory.get(), request, result.getFile());
    }
    return result;
  }


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
   * Disable the execution of pre processors if set to <code>true</code>.
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
   * Limit the number of result files to <code>limit</code> entries. By default, this is set to
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
   *               counted. Therefore, directories are only listed in results without
   *               offset.
   * @since 2.0.0
   */
  public BrowseCommandBuilder setOffset(int offset) {
    request.setOffset(offset);
    return this;
  }

  /**
   * Collapse folders with only one sub-folder until a folder is empty, contains files or has more than one sub-folder
   * and return the path to such folder as a single item.
   *
   * @param collapse {@code true} if folders with only one sub-folder should be collapsed, otherwise {@code false}.
   * @since 2.31.0
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


  static class CacheKey implements RepositoryCacheKey, Serializable
  {

      private static final long serialVersionUID = 8078650026812373524L;


    private final String repositoryId;

    private final BrowseCommandRequest request;


    public CacheKey(Repository repository, BrowseCommandRequest request)
    {
      this.repositoryId = repository.getId();
      this.request = request.clone();
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
