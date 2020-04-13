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

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.eclipse.jgit.transport.Transport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.util.NoneCachingWorkdirProvider;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.repository.util.WorkingCopy;

import java.io.File;
import java.io.IOException;

import static com.google.inject.util.Providers.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SimpleGitWorkdirFactoryTest extends AbstractGitCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  // keep this so that it will not be garbage collected (Transport keeps this in a week reference)
  private ScmTransportProtocol proto;
  private WorkdirProvider workdirProvider;

  @Before
  public void bindScmProtocol() throws IOException {
    HookContextFactory hookContextFactory = new HookContextFactory(mock(PreProcessorUtil.class));
    HookEventFacade hookEventFacade = new HookEventFacade(of(mock(RepositoryManager.class)), hookContextFactory);
    GitRepositoryHandler gitRepositoryHandler = mock(GitRepositoryHandler.class);
    proto = new ScmTransportProtocol(of(hookEventFacade), of(gitRepositoryHandler));
    Transport.register(proto);
    workdirProvider = new WorkdirProvider(temporaryFolder.newFolder());
  }

  @Test
  public void emptyPoolShouldCreateNewWorkdir() {
    SimpleGitWorkdirFactory factory = new SimpleGitWorkdirFactory(new NoneCachingWorkdirProvider(workdirProvider));
    File masterRepo = createRepositoryDirectory();

    try (WorkingCopy<Repository, Repository> workingCopy = factory.createWorkingCopy(createContext(), null)) {

      assertThat(workingCopy.getDirectory())
        .exists()
        .isNotEqualTo(masterRepo)
        .isDirectory();
      assertThat(new File(workingCopy.getWorkingRepository().getWorkTree(), "a.txt"))
        .exists()
        .isFile()
        .hasContent("a\nline for blame");
    }
  }

  @Test
  public void shouldCheckoutInitialBranch() {
    SimpleGitWorkdirFactory factory = new SimpleGitWorkdirFactory(new NoneCachingWorkdirProvider(workdirProvider));

    try (WorkingCopy<Repository, Repository> workingCopy = factory.createWorkingCopy(createContext(), "test-branch")) {
      assertThat(new File(workingCopy.getWorkingRepository().getWorkTree(), "a.txt"))
        .exists()
        .isFile()
        .hasContent("a and b");
    }
  }

  @Test
  public void shouldCheckoutDefaultBranch() {
    SimpleGitWorkdirFactory factory = new SimpleGitWorkdirFactory(new NoneCachingWorkdirProvider(workdirProvider));

    try (WorkingCopy<Repository, Repository> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      assertThat(new File(workingCopy.getWorkingRepository().getWorkTree(), "a.txt"))
        .exists()
        .isFile()
        .hasContent("a\nline for blame");
    }
  }

  @Test
  public void cloneFromPoolShouldNotBeReused() {
    SimpleGitWorkdirFactory factory = new SimpleGitWorkdirFactory(new NoneCachingWorkdirProvider(workdirProvider));

    File firstDirectory;
    try (WorkingCopy<Repository, Repository> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      firstDirectory = workingCopy.getDirectory();
    }
    try (WorkingCopy<Repository, Repository> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      File secondDirectory = workingCopy.getDirectory();
      assertThat(secondDirectory).isNotEqualTo(firstDirectory);
    }
  }

  @Test
  public void cloneFromPoolShouldBeDeletedOnClose() {
    SimpleGitWorkdirFactory factory = new SimpleGitWorkdirFactory(new NoneCachingWorkdirProvider(workdirProvider));

    File directory;
    try (WorkingCopy<Repository, Repository> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      directory = workingCopy.getWorkingRepository().getWorkTree();
    }
    assertThat(directory).doesNotExist();
  }
}
