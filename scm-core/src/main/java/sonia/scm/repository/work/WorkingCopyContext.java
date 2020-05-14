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

import sonia.scm.repository.Repository;

import java.io.File;

public class WorkingCopyContext<R, W> {
  private final String requestedBranch;
  private final Repository repository;
  private final SimpleWorkingCopyFactory.WorkingCopyInitializer<R, W> initializer;
  private final SimpleWorkingCopyFactory.WorkingCopyReclaimer<R, W> reclaimer;

  public WorkingCopyContext(String requestedBranch, Repository repository, SimpleWorkingCopyFactory.WorkingCopyInitializer<R, W> initializer, SimpleWorkingCopyFactory.WorkingCopyReclaimer<R, W> reclaimer) {
    this.requestedBranch = requestedBranch;
    this.repository = repository;
    this.initializer = initializer;
    this.reclaimer = reclaimer;
  }

  public Repository getScmRepository() {
    return repository;
  }

  public WorkingCopyPool.ParentAndClone<R, W> reclaim(File workdir) throws SimpleWorkingCopyFactory.ReclaimFailedException {
    return reclaimer.reclaim(workdir, requestedBranch);
  }

  public WorkingCopyPool.ParentAndClone<R, W> initialize(File workdir) {
    return initializer.initialize(workdir, requestedBranch);
  }
}
