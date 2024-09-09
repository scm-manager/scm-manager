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

package sonia.scm.repository.spi;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.PreProcessorUtil;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.repository.work.WorkingCopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.inject.util.Providers.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SimpleGitWorkingCopyFactoryTest extends AbstractGitCommandTestBase {

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
    proto = new ScmTransportProtocol(of(GitTestHelper.createConverterFactory()), of(hookEventFacade), of(gitRepositoryHandler));
    Transport.register(proto);
    workdirProvider = new WorkdirProvider(temporaryFolder.newFolder(), repositoryLocationResolver, false);
  }

  @Test
  public void emptyPoolShouldCreateNewWorkdir() {
    SimpleGitWorkingCopyFactory factory = new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
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
    SimpleGitWorkingCopyFactory factory = new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());

    try (WorkingCopy<Repository, Repository> workingCopy = factory.createWorkingCopy(createContext(), "test-branch")) {
      assertThat(new File(workingCopy.getWorkingRepository().getWorkTree(), "a.txt"))
        .exists()
        .isFile()
        .hasContent("a and b");
    }
  }

  @Test
  public void shouldCheckoutDefaultBranch() {
    SimpleGitWorkingCopyFactory factory = new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());

    try (WorkingCopy<Repository, Repository> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      assertThat(new File(workingCopy.getWorkingRepository().getWorkTree(), "a.txt"))
        .exists()
        .isFile()
        .hasContent("a\nline for blame");
    }
  }

  @Test
  public void cloneFromPoolShouldNotBeReused() {
    SimpleGitWorkingCopyFactory factory = new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());

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
    SimpleGitWorkingCopyFactory factory = new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());

    File directory;
    try (WorkingCopy<Repository, Repository> workingCopy = factory.createWorkingCopy(createContext(), null)) {
      directory = workingCopy.getWorkingRepository().getWorkTree();
    }
    assertThat(directory).doesNotExist();
  }

  @Test
  public void shouldReclaimCleanDirectoryWithSameBranch() throws Exception {
    SimpleGitWorkingCopyFactory factory = new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
    File workdir = createExistingClone(factory);

    factory.reclaim(createContext(), workdir, "master");

    assertBranchCheckedOutAndClean(workdir, "master");
  }

  @Test
  public void shouldReclaimCleanDirectoryConfiguredDefaultBranch() throws Exception {
    SimpleGitWorkingCopyFactory factory = new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
    File workdir = createExistingClone(factory);

    GitContext context = createContext();
    GitRepositoryConfig config = context.getConfig();
    config.setDefaultBranch("master");
    context.setConfig(config);
    factory.reclaim(context, workdir, null);

    assertBranchCheckedOutAndClean(workdir, "master");
  }

  @Test
  public void shouldReclaimCleanDirectoryGloballyConfiguredDefaultBranch() throws Exception {
    SimpleGitWorkingCopyFactory factory = new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
    File workdir = createExistingClone(factory);

    GitContext context = createContext();
    context.getGlobalConfig().setDefaultBranch("master");
    factory.reclaim(context, workdir, null);

    assertBranchCheckedOutAndClean(workdir, "master");
  }

  @Test
  public void shouldReclaimCleanDirectoryWithOtherBranch() throws Exception {
    SimpleGitWorkingCopyFactory factory = new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
    File workdir = createExistingClone(factory);

    factory.reclaim(createContext(), workdir, "test-branch");

    assertBranchCheckedOutAndClean(workdir, "test-branch");
  }

  @Test
  public void shouldReclaimDirectoryWithDeletedFileInIndex() throws Exception {
    SimpleGitWorkingCopyFactory factory = new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
    File workdir = createExistingClone(factory);
    Git.open(workdir).rm().addFilepattern("a.txt").call();

    factory.reclaim(createContext(), workdir, "master");

    assertBranchCheckedOutAndClean(workdir, "master");
  }

  @Test
  public void shouldReclaimDirectoryWithDeletedFileInDirectory() throws Exception {
    SimpleGitWorkingCopyFactory factory = new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
    File workdir = createExistingClone(factory);
    Files.delete(workdir.toPath().resolve("a.txt"));

    factory.reclaim(createContext(), workdir, "master");

    assertBranchCheckedOutAndClean(workdir, "master");
  }

  @Test
  public void shouldReclaimDirectoryWithAdditionalFileInDirectory() throws Exception {
    SimpleGitWorkingCopyFactory factory = new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(workdirProvider), new SimpleMeterRegistry());
    File workdir = createExistingClone(factory);
    Path newDirectory = workdir.toPath().resolve("new");
    Files.createDirectories(newDirectory);
    Files.createFile(newDirectory.resolve("newFile"));

    factory.reclaim(createContext(), workdir, "master");

    assertBranchCheckedOutAndClean(workdir, "master");
    assertThat(newDirectory).doesNotExist();
  }

  public File createExistingClone(SimpleGitWorkingCopyFactory factory) throws Exception {
    File workdir = temporaryFolder.newFolder();
    extract(workdir, "sonia/scm/repository/spi/scm-git-spi-test-workdir.zip");
    Git.open(workdir).remoteSetUrl().setRemoteUri(new URIish(factory.createScmTransportProtocolUri(repositoryDirectory))).setRemoteName("origin").call();
    return workdir;
  }

  private void assertBranchCheckedOutAndClean(File workdir, String expectedBranch) throws Exception {
    Git git = Git.open(workdir);
    assertThat(git.getRepository().getBranch()).isEqualTo(expectedBranch);
    Status workdirStatus = git.status().call();
    assertStatusClean(workdirStatus);
  }

  private void assertStatusClean(Status workdirStatus) {
    assertThat(workdirStatus.getAdded()).isEmpty();
    assertThat(workdirStatus.getChanged()).isEmpty();
    assertThat(workdirStatus.getConflicting()).isEmpty();
    assertThat(workdirStatus.getIgnoredNotInIndex()).isEmpty();
    assertThat(workdirStatus.getMissing()).isEmpty();
    assertThat(workdirStatus.getModified()).isEmpty();
    assertThat(workdirStatus.getRemoved()).isEmpty();
    assertThat(workdirStatus.getUntracked()).isEmpty();
    assertThat(workdirStatus.getUncommittedChanges()).isEmpty();
  }
}
