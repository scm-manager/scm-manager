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

package sonia.scm.repository.util;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.IOUtil;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachingAllWorkdirProvider implements CacheSupportingWorkdirProvider {

  private static final Logger LOG = LoggerFactory.getLogger(CachingAllWorkdirProvider.class);

  private final Map<String, File> workdirs = new ConcurrentHashMap<>();

  private final WorkdirProvider workdirProvider;

  @Inject
  public CachingAllWorkdirProvider(WorkdirProvider workdirProvider) {
    this.workdirProvider = workdirProvider;
  }

  @Override
  public <R, W, C> SimpleWorkdirFactory.ParentAndClone<R, W> getWorkdir(CreateWorkdirContext<R, W, C> createWorkdirContext) throws IOException {
    String id = createWorkdirContext.getScmRepository().getId();
    File existingWorkdir = workdirs.remove(id);
    if (existingWorkdir != null) {
      Stopwatch stopwatch = Stopwatch.createStarted();
      try {
        SimpleWorkdirFactory.ParentAndClone<R, W> reclaimed = createWorkdirContext.getReclaimer().reclaim(existingWorkdir);
        LOG.debug("reclaimed workdir for {} in path {} in {}", createWorkdirContext.getScmRepository().getNamespaceAndName(), existingWorkdir, stopwatch.stop());
        return reclaimed;
      } catch (SimpleWorkdirFactory.ReclaimFailedException e) {
        LOG.debug("failed to relaim workdir for {} in path {} in {}", createWorkdirContext.getScmRepository().getNamespaceAndName(), existingWorkdir, stopwatch.stop(), e);
        deleteWorkdir(existingWorkdir);
      }
    }
    return createNewWorkdir(createWorkdirContext);
  }

  private  <R, W> SimpleWorkdirFactory.ParentAndClone<R, W> createNewWorkdir(CreateWorkdirContext<R, W, ?> createWorkdirContext) throws IOException {
    String id = createWorkdirContext.getScmRepository().getId();
    Stopwatch stopwatch = Stopwatch.createStarted();
    File newWorkdir = workdirProvider.createNewWorkdir();
    workdirs.put(id, newWorkdir);
    SimpleWorkdirFactory.ParentAndClone<R, W> parentAndClone = createWorkdirContext.getInitializer().initialize(newWorkdir);
    LOG.debug("initialized new workdir for {} in path {} in {}", createWorkdirContext.getScmRepository().getNamespaceAndName(), newWorkdir, stopwatch.stop());
    return parentAndClone;
  }

  @Override
  public void contextClosed(CreateWorkdirContext<?, ?, ?> createWorkdirContext, File workdir) throws IOException {
    String id = createWorkdirContext.getScmRepository().getId();
    File putResult = workdirs.putIfAbsent(id, workdir);
    if (putResult != null && putResult != workdir) {
      deleteWorkdir(workdir);
    }
  }

  private void deleteWorkdir(File existingWorkdir) throws IOException {
    IOUtil.delete(existingWorkdir, true);
  }
}
