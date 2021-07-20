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

package sonia.scm.repository.work;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.IOUtil;

import javax.inject.Inject;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.Integer.getInteger;

/**
 * This class is a simple implementation of the {@link WorkingCopyPool} to demonstrate,
 * how caching can work in an LRU style. For the first time a {@link WorkingCopy} is
 * requested for a repository with {@link #getWorkingCopy(SimpleWorkingCopyFactory.WorkingCopyContext)},
 * this implementation fetches a new directory from the {@link WorkdirProvider}.
 * On {@link #contextClosed(SimpleWorkingCopyFactory.WorkingCopyContext, File)},
 * the directory is not deleted, but put into a cache with the repository id as key.
 * When a working copy is requested with {@link #getWorkingCopy(SimpleWorkingCopyFactory.WorkingCopyContext)}
 * for a repository with such an existing directory, it is taken from the map, reclaimed and
 * returned as {@link WorkingCopy}.
 * If for one repository a working copy is requested, while another is in use already,
 * a second directory is requested from the {@link WorkdirProvider} for the second one.
 * If a context is closed with {@link #contextClosed(SimpleWorkingCopyFactory.WorkingCopyContext, File)}
 * and there already is a directory stored in the map for the repository,
 * the directory from the closed context simply is deleted.
 * The number of directories cached is limited. By default, directories are cached for
 * {@value DEFAULT_WORKING_COPY_POOL_SIZE} repositories. This can be changes with the system
 * property '{@value WORKING_COPY_POOL_SIZE_PROPERTY}' (if this is set to zero, no caching will
 * take place; to cache the directories for each repository without eviction simply set this to a
 * high enough value).
 * <br>
 * The usage of this pool has to be enabled by setting the system property `scm.workingCopyPoolStrategy`
 * to 'sonia.scm.repository.work.SimpleCachingWorkingCopyPool'.
 * <br>
 * In general, this implementation should speed up modifications inside SCM-Manager performed by
 * the editor plugin or the review plugin, but one has to take into
 * account, that the space needed for repositories is multiplied. So you have to make sure, that
 * there is enough space for clones of the repository.
 * <br>
 * Possible enhancements:
 * <ul>
 *   <li>Monitoring of times</li>
 *   <li>Allow multiple cached directories for busy repositories (possibly taking initial branches into account)</li>
 *   <li>Measure allocated disk space and set a limit</li>
 *   <li>Remove directories not used for a longer time</li>
 *   <li>Wait for a cached directory on parallel requests</li>
 * </ul>
 */
public class SimpleCachingWorkingCopyPool implements WorkingCopyPool {

  public static final int DEFAULT_WORKING_COPY_POOL_SIZE = 5;
  public static final String WORKING_COPY_POOL_SIZE_PROPERTY = "scm.workingCopyPoolSize";

  private static final Logger LOG = LoggerFactory.getLogger(SimpleCachingWorkingCopyPool.class);

  private final WorkdirProvider workdirProvider;
  private final LinkedHashMap<String, File> workdirs;
  private final boolean cacheEnabled;

  @Inject
  public SimpleCachingWorkingCopyPool(WorkdirProvider workdirProvider) {
    this(getInteger(WORKING_COPY_POOL_SIZE_PROPERTY, DEFAULT_WORKING_COPY_POOL_SIZE), workdirProvider);
  }

  @VisibleForTesting
  SimpleCachingWorkingCopyPool(int size, WorkdirProvider workdirProvider) {
    this.workdirProvider = workdirProvider;
    this.workdirs = new LinkedHashMap<String, File>(size) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, File> eldest) {
        if (size() > size) {
          deleteWorkdir(eldest.getValue());
          return true;
        }
        return false;
      }
    };
    cacheEnabled = size > 0;
  }

  @Override
  public <R, W> WorkingCopy<R, W> getWorkingCopy(SimpleWorkingCopyFactory<R, W, ?>.WorkingCopyContext workingCopyContext) {
    String id = workingCopyContext.getScmRepository().getId();
    File existingWorkdir;
    synchronized (workdirs) {
      existingWorkdir = workdirs.remove(id);
    }
    if (existingWorkdir != null) {
      Stopwatch stopwatch = Stopwatch.createStarted();
      try {
        WorkingCopy<R, W> reclaimed = workingCopyContext.reclaim(existingWorkdir);
        LOG.debug("reclaimed workdir for {} in path {} in {}", workingCopyContext.getScmRepository(), existingWorkdir, stopwatch.stop());
        return reclaimed;
      } catch (SimpleWorkingCopyFactory.ReclaimFailedException e) {
        LOG.debug("failed to reclaim workdir for {} in path {} in {}", workingCopyContext.getScmRepository(), existingWorkdir, stopwatch.stop(), e);
        deleteWorkdir(existingWorkdir);
      }
    }
    return createNewWorkingCopy(workingCopyContext);
  }

  private <R, W> WorkingCopy<R, W> createNewWorkingCopy(SimpleWorkingCopyFactory<R, W, ?>.WorkingCopyContext workingCopyContext) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    File newWorkdir = workdirProvider.createNewWorkdir(workingCopyContext.getScmRepository().getId());
    WorkingCopy<R, W> parentAndClone = workingCopyContext.initialize(newWorkdir);
    LOG.debug("initialized new workdir for {} in path {} in {}", workingCopyContext.getScmRepository(), newWorkdir, stopwatch.stop());
    return parentAndClone;
  }

  @Override
  public void contextClosed(SimpleWorkingCopyFactory<?, ?, ?>.WorkingCopyContext workingCopyContext, File workdir) {
    if (!cacheEnabled) {
      deleteWorkdir(workdir);
      return;
    }
    String id = workingCopyContext.getScmRepository().getId();
    synchronized (workdirs) {
      File cachedWorkdir = workdirs.putIfAbsent(id, workdir);
      if (cachedWorkdir != null && cachedWorkdir != workdir) {
        deleteWorkdir(workdir);
      } else if (cachedWorkdir != null) {
        moveToTopInCache(id, cachedWorkdir);
      }
    }
  }

  private void moveToTopInCache(String id, File workDir) {
    workdirs.remove(id);
    workdirs.put(id, workDir);
  }

  @Override
  public void shutdown() {
    workdirs.values().parallelStream().forEach(this::deleteWorkdir);
    workdirs.clear();
  }

  private void deleteWorkdir(File workdir) {
    LOG.debug("deleting old workdir {}", workdir);
    if (workdir.exists()) {
      IOUtil.deleteSilently(workdir);
    }
  }
}
