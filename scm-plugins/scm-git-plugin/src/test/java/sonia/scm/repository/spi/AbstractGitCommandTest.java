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
        new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(new WorkdirProvider(repositoryLocationResolver)), new SimpleMeterRegistry()),
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
