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

import com.aragost.javahg.Repository;
import com.google.inject.util.Providers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.work.SimpleCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.repository.work.WorkingCopy;
import sonia.scm.web.HgRepositoryEnvironmentBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleHgWorkingCopyFactoryTest extends AbstractHgCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private WorkdirProvider workdirProvider;

  private SimpleHgWorkingCopyFactory workingCopyFactory;

  @Before
  public void bindScmProtocol() throws IOException {
    workdirProvider = new WorkdirProvider(temporaryFolder.newFolder());
    HgHookManager hookManager = HgTestUtil.createHookManager();
    HgRepositoryEnvironmentBuilder environmentBuilder = new HgRepositoryEnvironmentBuilder(handler, hookManager);
    workingCopyFactory = new SimpleHgWorkingCopyFactory(Providers.of(environmentBuilder), new SimpleCachingWorkingCopyPool(workdirProvider)) {
      @Override
      public void configure(com.aragost.javahg.commands.PullCommand pullCommand) {
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
    assertThat(cachedWorkingCopy.getDirectory().toPath().resolve("newDir")).isEmptyDirectory();
  }
}
