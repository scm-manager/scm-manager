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

package sonia.scm.repository.spi;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.javahg.BaseRepository;
import org.javahg.Repository;
import org.javahg.commands.BranchCommand;
import org.javahg.commands.RemoveCommand;
import org.javahg.commands.StatusCommand;
import org.javahg.commands.results.StatusResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.SimpleCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.repository.work.WorkingCopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleHgWorkingCopyFactoryTest extends AbstractHgCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private MeterRegistry meterRegistry = new SimpleMeterRegistry();

  private WorkdirProvider workdirProvider;

  private SimpleHgWorkingCopyFactory workingCopyFactory;

  @Before
  public void bindScmProtocol() throws IOException {
    workdirProvider = new WorkdirProvider(temporaryFolder.newFolder(), repositoryLocationResolver, false);
    workingCopyFactory = new SimpleHgWorkingCopyFactory(new SimpleCachingWorkingCopyPool(5, workdirProvider, meterRegistry), new SimpleMeterRegistry()) {
      @Override
      public void configure(org.javahg.commands.PullCommand pullCommand) {
        // we do not want to configure http hooks in this unit test
      }
    };
  }

  @Test
  public void shouldSwitchBranch() {
    WorkingCopy<Repository, Repository> workingCopy = workingCopyFactory.createWorkingCopy(cmdContext, "default");

    File initialDirectory = workingCopy.getDirectory();
    workingCopy.close();

    assertThat(initialDirectory).exists();
    assertThat(initialDirectory.toPath().resolve("f.txt")).exists();

    WorkingCopy<Repository, Repository> cachedWorkingCopy = workingCopyFactory.createWorkingCopy(cmdContext, "test-branch");
    assertThat(cachedWorkingCopy.getDirectory()).isEqualTo(initialDirectory);
    assertThat(cachedWorkingCopy.getDirectory().toPath().resolve("f.txt")).doesNotExist();
  }

  @Test
  public void shouldReplaceFileWithContentFromNewBranch() throws IOException {
    WorkingCopy<Repository, Repository> workingCopy = workingCopyFactory.createWorkingCopy(cmdContext, "test-branch");

    File initialDirectory = workingCopy.getDirectory();
    Path fileToBeReplaced = initialDirectory.toPath().resolve("f.txt");
    Files.createFile(fileToBeReplaced);
    Files.write(fileToBeReplaced, Collections.singleton("some content"));

    workingCopy.close();

    assertThat(initialDirectory).exists();
    assertThat(fileToBeReplaced).hasContent("some content");

    WorkingCopy<Repository, Repository> cachedWorkingCopy = workingCopyFactory.createWorkingCopy(cmdContext, "default");
    assertThat(cachedWorkingCopy.getDirectory()).isEqualTo(initialDirectory);
    assertThat(cachedWorkingCopy.getDirectory().toPath().resolve("f.txt")).exists();
    assertThat(fileToBeReplaced).hasContent("f");
  }

  @Test
  public void shouldDeleteUntrackedFile() throws IOException {
    WorkingCopy<Repository, Repository> workingCopy = workingCopyFactory.createWorkingCopy(cmdContext, "test-branch");

    File initialDirectory = workingCopy.getDirectory();
    Path fileToBeDeleted = initialDirectory.toPath().resolve("x.txt");
    Files.createFile(fileToBeDeleted);
    Files.write(fileToBeDeleted, Collections.singleton("some content"));

    workingCopy.close();

    assertThat(initialDirectory).exists();
    assertThat(fileToBeDeleted).hasContent("some content");

    WorkingCopy<Repository, Repository> cachedWorkingCopy = workingCopyFactory.createWorkingCopy(cmdContext, "default");
    assertThat(cachedWorkingCopy.getDirectory()).isEqualTo(initialDirectory);
    assertThat(cachedWorkingCopy.getDirectory().toPath().resolve("x.txt")).doesNotExist();
  }

  @Test
  public void shouldDeleteUntrackedDirectory() throws IOException {
    WorkingCopy<Repository, Repository> workingCopy = workingCopyFactory.createWorkingCopy(cmdContext, "test-branch");

    File initialDirectory = workingCopy.getDirectory();
    Path directoryToBeDeleted = initialDirectory.toPath().resolve("newDir");
    Files.createDirectories(directoryToBeDeleted);
    Path fileToBeDeleted = directoryToBeDeleted.resolve("y.txt");
    Files.createFile(fileToBeDeleted);
    Files.write(fileToBeDeleted, Collections.singleton("some content"));

    workingCopy.close();

    assertThat(initialDirectory).exists();
    assertThat(fileToBeDeleted).hasContent("some content");

    WorkingCopy<Repository, Repository> cachedWorkingCopy = workingCopyFactory.createWorkingCopy(cmdContext, "default");
    assertThat(cachedWorkingCopy.getDirectory()).isEqualTo(initialDirectory);
    assertThat(cachedWorkingCopy.getDirectory().toPath().resolve("newDir")).doesNotExist();
  }

  @Test
  public void shouldReclaimCleanDirectoryWithSameBranch() throws Exception {
    SimpleHgWorkingCopyFactory factory = new SimpleHgWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
    File workdir = createExistingClone(factory);

    factory.reclaim(cmdContext, workdir, "default");

    assertBranchCheckedOutAndClean(workdir, "default");
  }

  @Test
  public void shouldReclaimCleanDirectoryWithDefaultBranch() throws Exception {
    SimpleHgWorkingCopyFactory factory = new SimpleHgWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
    File workdir = createExistingClone(factory);

    factory.reclaim(cmdContext, workdir, null);

    assertBranchCheckedOutAndClean(workdir, "default");
  }

  @Test
  public void shouldReclaimCleanDirectoryWithOtherBranch() throws Exception {
    SimpleHgWorkingCopyFactory factory = new SimpleHgWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
    File workdir = createExistingClone(factory);

    factory.reclaim(cmdContext, workdir, "test-branch");

    assertBranchCheckedOutAndClean(workdir, "test-branch");
  }

  @Test
  public void shouldReclaimDirectoryWithDeletedFileInIndex() throws Exception {
    SimpleHgWorkingCopyFactory factory = new SimpleHgWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
    File workdir = createExistingClone(factory);

    RemoveCommand.on(Repository.open(workdir)).execute("a.txt");

    factory.reclaim(cmdContext, workdir, "default");

    assertBranchCheckedOutAndClean(workdir, "default");
  }

  @Test
  public void shouldReclaimDirectoryWithDeletedFileInDirectory() throws Exception {
    SimpleHgWorkingCopyFactory factory = new SimpleHgWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
    File workdir = createExistingClone(factory);

    RemoveCommand.on(Repository.open(workdir)).execute("c");

    factory.reclaim(cmdContext, workdir, "default");

    assertBranchCheckedOutAndClean(workdir, "default");
  }

  @Test
  public void shouldReclaimDirectoryWithAdditionalFileInDirectory() throws Exception {
    SimpleHgWorkingCopyFactory factory = new SimpleHgWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
    File workdir = createExistingClone(factory);

    Path newDirectory = workdir.toPath().resolve("new");
    Files.createDirectories(newDirectory);
    Files.createFile(newDirectory.resolve("newFile"));

    factory.reclaim(cmdContext, workdir, "default");

    assertBranchCheckedOutAndClean(workdir, "default");
    assertThat(newDirectory).doesNotExist();
  }

  private void assertBranchCheckedOutAndClean(File workdir, String expectedBranch) {
    BaseRepository repository = Repository.open(workdir);
    StatusResult statusResult = StatusCommand.on(repository).execute();
    assertThat(statusResult.getAdded()).isEmpty();
    assertThat(statusResult.getCopied()).isEmpty();
    assertThat(statusResult.getIgnored()).isEmpty();
    assertThat(statusResult.getMissing()).isEmpty();
    assertThat(statusResult.getModified()).isEmpty();
    assertThat(statusResult.getRemoved()).isEmpty();
    assertThat(statusResult.getUnknown()).isEmpty();
    assertThat(BranchCommand.on(repository).get()).isEqualTo(expectedBranch);
  }

  public File createExistingClone(SimpleHgWorkingCopyFactory factory) throws Exception {
    File workdir = temporaryFolder.newFolder();
    extract(workdir, "sonia/scm/repository/spi/scm-hg-spi-workdir-test.zip");
    Files.write(workdir.toPath().resolve(".hg").resolve("hgrc"), Arrays.asList("[paths]", "default = " + repositoryDirectory.getAbsolutePath()));
    return workdir;
  }
}
