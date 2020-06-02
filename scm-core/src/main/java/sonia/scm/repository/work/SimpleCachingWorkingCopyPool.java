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

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.IOUtil;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is a simple implementation of the {@link WorkingCopyPool} to demonstrate,
 * how caching can work in the simplest way. For the first time a {@link WorkingCopy} is
 * requested for a repository with {@link #getWorkingCopy(SimpleWorkingCopyFactory.WorkingCopyContext)},
 * this implementation fetches a new directory from the {@link WorkdirProvider}.
 * On {@link #contextClosed(SimpleWorkingCopyFactory.WorkingCopyContext, File)},
 * the directory is not deleted, but put into a map with the repository id as key.
 * When a working copy is requested with {@link #getWorkingCopy(SimpleWorkingCopyFactory.WorkingCopyContext)}
 * for a repository with such an existing directory, it is taken from the map, reclaimed and
 * returned as {@link WorkingCopy}.
 * If for one repository a working copy is requested, while another is in use already,
 * a second directory is requested from the {@link WorkdirProvider} for the second one.
 * If a context is closed with {@link #contextClosed(SimpleWorkingCopyFactory.WorkingCopyContext, File)}
 * and there already is a directory stored in the map for the repository,
 * the directory from the closed context simply is deleted.
 * <br>
 * In general, this implementation should speed up things a bit, but one has to take into
 * account, that there is no monitoring of diskspace. So you have to make sure, that
 * there is enough space for a clone of each repository in the working dir.
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

  private static final Logger LOG = LoggerFactory.getLogger(SimpleCachingWorkingCopyPool.class);

  private final Map<String, File> workdirs = new ConcurrentHashMap<>();

  private final WorkdirProvider workdirProvider;

  @Inject
  public SimpleCachingWorkingCopyPool(WorkdirProvider workdirProvider) {
    this.workdirProvider = workdirProvider;
  }

  @Override
  public <R, W> WorkingCopy<R, W> getWorkingCopy(SimpleWorkingCopyFactory<R, W, ?>.WorkingCopyContext workingCopyContext) {
    String id = workingCopyContext.getScmRepository().getId();
    File existingWorkdir = workdirs.remove(id);
    if (existingWorkdir != null) {
      Stopwatch stopwatch = Stopwatch.createStarted();
      try {
        WorkingCopy<R, W> reclaimed = workingCopyContext.reclaim(existingWorkdir);
        LOG.debug("reclaimed workdir for {} in path {} in {}", workingCopyContext.getScmRepository().getNamespaceAndName(), existingWorkdir, stopwatch.stop());
        return reclaimed;
      } catch (SimpleWorkingCopyFactory.ReclaimFailedException e) {
        LOG.debug("failed to reclaim workdir for {} in path {} in {}", workingCopyContext.getScmRepository().getNamespaceAndName(), existingWorkdir, stopwatch.stop(), e);
        deleteWorkdir(existingWorkdir);
      }
    }
    return createNewWorkingCopy(workingCopyContext);
  }

  private <R, W> WorkingCopy<R, W> createNewWorkingCopy(SimpleWorkingCopyFactory<R, W, ?>.WorkingCopyContext workingCopyContext) {
    Stopwatch stopwatch = Stopwatch.createStarted();
    File newWorkdir = workdirProvider.createNewWorkdir();
    WorkingCopy<R, W> parentAndClone = workingCopyContext.initialize(newWorkdir);
    LOG.debug("initialized new workdir for {} in path {} in {}", workingCopyContext.getScmRepository().getNamespaceAndName(), newWorkdir, stopwatch.stop());
    return parentAndClone;
  }

  @Override
  public void contextClosed(SimpleWorkingCopyFactory<?, ?, ?>.WorkingCopyContext workingCopyContext, File workdir) {
    String id = workingCopyContext.getScmRepository().getId();
    File putResult = workdirs.putIfAbsent(id, workdir);
    if (putResult != null && putResult != workdir) {
      deleteWorkdir(workdir);
    }
  }

  @Override
  public void shutdown() {
    workdirs.values().parallelStream().forEach(this::deleteWorkdir);
    workdirs.clear();
  }

  private void deleteWorkdir(File workdir) {
    if (workdir.exists()) {
      IOUtil.deleteSilently(workdir);
    }
  }
}
