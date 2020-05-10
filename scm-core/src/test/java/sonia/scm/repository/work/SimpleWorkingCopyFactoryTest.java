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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.Repository;
import sonia.scm.util.IOUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SimpleWorkingCopyFactoryTest {

  private static final Repository REPOSITORY = new Repository("1", "git", "space", "X");

  private final Closeable parent = mock(Closeable.class);
  private final Closeable clone = mock(Closeable.class);

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private SimpleWorkingCopyFactory<Closeable, Closeable, Context> simpleWorkingCopyFactory;

  private String initialBranchForLastCloneCall;

  private boolean workdirIsCached = false;
  private File workdir;

  @Before
  public void initFactory() throws IOException {
    WorkdirProvider workdirProvider = new WorkdirProvider(temporaryFolder.newFolder());
    WorkingCopyPool configurableTestWorkingCopyPool = new WorkingCopyPool() {
      @Override
      public <R, W, C extends Supplier<Repository>> ParentAndClone<R, W> getWorkingCopy(WorkingCopyContext<R, W, C> context) throws WorkingCopyFailedException {
        workdir = workdirProvider.createNewWorkdir();
        return context.getInitializer().initialize(workdir);
      }

      @Override
      public void contextClosed(WorkingCopyContext<?, ?, ?> createWorkdirContext, File workdir) {
        if (!workdirIsCached) {
          IOUtil.deleteSilently(workdir);
        }
      }

      @Override
      public void shutdown() {
      }
    };
    simpleWorkingCopyFactory = new SimpleWorkingCopyFactory<Closeable, Closeable, Context>(configurableTestWorkingCopyPool) {
      @Override
      protected void closeRepository(Closeable repository) throws IOException {
        repository.close();
      }

      @Override
      protected WorkingCopyPool.ParentAndClone<Closeable, Closeable> reclaimRepository(Context context, File target, String initialBranch) {
        throw new UnsupportedOperationException();
      }

      @Override
      protected void closeWorkingCopy(Closeable workingCopy) throws Exception {
        workingCopy.close();
      }

      @Override
      protected WorkingCopyPool.ParentAndClone<Closeable, Closeable> cloneRepository(Context context, File target, String initialBranch) {
        initialBranchForLastCloneCall = initialBranch;
        return new WorkingCopyPool.ParentAndClone<>(parent, clone, target);
      }
    };
  }

  @Test
  public void shouldCreateParentAndClone() {
    Context context = new Context();
    try (WorkingCopy<Closeable, Closeable> workingCopy = simpleWorkingCopyFactory.createWorkingCopy(context, null)) {
      assertThat(workingCopy.getCentralRepository()).isSameAs(parent);
      assertThat(workingCopy.getWorkingRepository()).isSameAs(clone);
    }
  }

  @Test
  public void shouldCloseParent() throws IOException {
    Context context = new Context();
    try (WorkingCopy<Closeable, Closeable> workingCopy = simpleWorkingCopyFactory.createWorkingCopy(context, null)) {}

    verify(parent).close();
  }

  @Test
  public void shouldCloseClone() throws IOException {
    Context context = new Context();
    try (WorkingCopy<Closeable, Closeable> workingCopy = simpleWorkingCopyFactory.createWorkingCopy(context, null)) {}

    verify(clone).close();
  }

  @Test
  public void shouldDeleteWorkdirIfNotCached() {
    Context context = new Context();
    try (WorkingCopy<Closeable, Closeable> workingCopy = simpleWorkingCopyFactory.createWorkingCopy(context, null)) {}

    assertThat(workdir).doesNotExist();
  }

  @Test
  public void shouldNotDeleteWorkdirIfCached() {
    Context context = new Context();
    workdirIsCached = true;
    try (WorkingCopy<Closeable, Closeable> workingCopy = simpleWorkingCopyFactory.createWorkingCopy(context, null)) {}

    assertThat(workdir).exists();
  }

  @Test
  public void shouldPropagateInitialBranch() {
    Context context = new Context();
    try (WorkingCopy<Closeable, Closeable> workingCopy = simpleWorkingCopyFactory.createWorkingCopy(context, "some")) {
      assertThat(initialBranchForLastCloneCall).isEqualTo("some");
    }
  }

  private static class Context implements Supplier<Repository> {
    @Override
    public Repository get() {
      return REPOSITORY;
    }
  }
}
