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
import org.awaitility.Awaitility;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class AbstractGitCommandTest extends AbstractGitCommandTestBase {

  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();
  @Rule
  public TemporaryFolder cloneFolder = new TemporaryFolder();

  private final CountDownLatch createdWorkingCopyLatch = new CountDownLatch(1);
  private final CountDownLatch createdConflictingCommitLatch = new CountDownLatch(1);
  private boolean gotConflict = false;

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-test.zip";
  }

  @Test
  public void shouldNotOverwriteConcurrentCommit() throws GitAPIException, IOException, InterruptedException {
    GitContext context = createContext();
    SimpleGitCommand command = new SimpleGitCommand(context);
    Repository repo = context.open();

    new Thread(() -> {
      try {
        command.doSomething();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    Git clone = Git
      .cloneRepository()
      .setURI(repo.getDirectory().toString())
      .setDirectory(cloneFolder.newFolder())
      .call();

    // somehow we have to wait here, though we don't know why
    Thread.sleep(1000);

    createdWorkingCopyLatch.await();
    createCommit(clone);
    clone.push().call();
    createdConflictingCommitLatch.countDown();

    Awaitility.await().until(() -> gotConflict);
  }

  private class SimpleGitCommand extends AbstractGitCommand {

    SimpleGitCommand(GitContext context) {
      super(context);
    }

    void doSomething() {
      inClone(
        git -> new Worker(context, repository, git),
        new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(new WorkdirProvider(null, repositoryLocationResolver)), new SimpleMeterRegistry()),
        "master"
      );
    }
  }

  private synchronized void createCommit(Git git) throws GitAPIException {
    git.commit().setMessage("Add new commit").call();
  }

  private class Worker extends AbstractGitCommand.GitCloneWorker<Void> {

    private final Git git;

    private Worker(GitContext context, sonia.scm.repository.Repository repository, Git git) {
      super(git, context, repository);
      this.git = git;
    }

    @Override
    Void run() {
      try {
        createCommit(git);
        createdWorkingCopyLatch.countDown();
        createdConflictingCommitLatch.await();
        push("master");
      } catch (ConcurrentModificationException e) {
        gotConflict = true;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return null;
    }
  }
}
