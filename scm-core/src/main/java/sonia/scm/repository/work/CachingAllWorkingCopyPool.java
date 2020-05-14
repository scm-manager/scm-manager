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

public class CachingAllWorkingCopyPool implements WorkingCopyPool {

  private static final Logger LOG = LoggerFactory.getLogger(CachingAllWorkingCopyPool.class);

  private final Map<String, File> workdirs = new ConcurrentHashMap<>();

  private final WorkdirProvider workdirProvider;

  @Inject
  public CachingAllWorkingCopyPool(WorkdirProvider workdirProvider) {
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
