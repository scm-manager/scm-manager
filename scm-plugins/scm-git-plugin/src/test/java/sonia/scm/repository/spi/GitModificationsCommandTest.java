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

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.Modifications;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class GitModificationsCommandTest extends AbstractRemoteCommandTestBase {

  private GitModificationsCommand incomingModificationsCommand;
  private GitModificationsCommand outgoingModificationsCommand;

  @Mock
  private LfsLoader lfsLoader;

  @Before
  public void init() {
    incomingModificationsCommand = new GitModificationsCommand(Mockito.spy(new GitContext(incomingDirectory, incomingRepository, null, new GitConfig())));
    outgoingModificationsCommand = new GitModificationsCommand(Mockito.spy(new GitContext(outgoingDirectory, outgoingRepository, null, new GitConfig())));
  }

  @Test
  public void shouldNotCloseRepository() throws IOException, GitAPIException {
    write(outgoing, outgoingDirectory, "a.txt", "bal bla");
    RevCommit addedFileCommit = commit(outgoing, "add file");
    String revision = addedFileCommit.getName();

    final GitModificationsCommand command = new GitModificationsCommand(Mockito.spy(new GitContext(outgoingDirectory, outgoingRepository, null, new GitConfig())));
    final Repository repository = Mockito.spy(command.context.open());
    command.context.setGitRepository(repository);

    command.getModifications(revision);

    Mockito.verify(command.context, times(3)).open();
    Mockito.verify(repository, never()).close();

  }

  @Test
  public void shouldReadAddedFiles() throws Exception {
    write(outgoing, outgoingDirectory, "a.txt", "bal bla");
    RevCommit addedFileCommit = commit(outgoing, "add file");
    String revision = addedFileCommit.getName();
    Consumer<Modifications> assertModifications = assertAddedFiles("a.txt");
    assertModifications.accept(outgoingModificationsCommand.getModifications(revision));
    pushOutgoingAndPullIncoming();
    assertModifications.accept(incomingModificationsCommand.getModifications(revision));
  }

  @Test
  public void shouldReadModifiedFiles() throws Exception {
    write(outgoing, outgoingDirectory, "a.txt", "bal bla");
    commit(outgoing, "add file");
    write(outgoing, outgoingDirectory, "a.txt", "modified content");
    RevCommit modifiedFileCommit = commit(outgoing, "modify file");
    String revision = modifiedFileCommit.getName();
    Consumer<Modifications> assertModifications = assertModifiedFiles("a.txt");
    assertModifications.accept(outgoingModificationsCommand.getModifications(revision));
    pushOutgoingAndPullIncoming();
    assertModifications.accept(incomingModificationsCommand.getModifications(revision));
  }

  @Test
  public void shouldReadRemovedFiles() throws Exception {
    String fileName = "a.txt";
    write(outgoing, outgoingDirectory, fileName, "bal bla");
    commit(outgoing, "add file");
    File file = new File(outgoingDirectory, fileName);
    file.delete();
    outgoing.rm().addFilepattern(fileName).call();
    RevCommit removedFileCommit = commit(outgoing, "remove file");
    String revision = removedFileCommit.getName();
    Consumer<Modifications> assertModifications = assertRemovedFiles(fileName);
    pushOutgoingAndPullIncoming();
    assertModifications.accept(incomingModificationsCommand.getModifications(revision));
    assertModifications.accept(outgoingModificationsCommand.getModifications(revision));
  }

  @Test
  public void shouldReadRenamedFiles() throws Exception {
    String originalFile = "a.txt";
    write(outgoing, outgoingDirectory, originalFile, "bal bla");
    commit(outgoing, "add file");
    write(outgoing, outgoingDirectory, "b.txt", "bal bla");
    File file = new File(outgoingDirectory, originalFile);
    file.delete();
    outgoing.rm().addFilepattern(originalFile).call();

    RevCommit modifiedFileCommit = commit(outgoing, "rename file");
    String revision = modifiedFileCommit.getName();

    Consumer<Modifications> assertModifications = assertRenamedFiles("b.txt");
    assertModifications.accept(outgoingModificationsCommand.getModifications(revision));
    pushOutgoingAndPullIncoming();
    assertModifications.accept(incomingModificationsCommand.getModifications(revision));
  }

  @Test
  public void shouldFindModificationsBetweenRevisions() throws Exception {
    write(outgoing, outgoingDirectory, "a.txt", "bal bla");
    write(outgoing, outgoingDirectory, "d.txt", "some file to be renamed");
    RevCommit baseCommit = commit(outgoing, "add files");

    write(outgoing, outgoingDirectory, "a.txt", "modified content");
    commit(outgoing, "modify file");
    write(outgoing, outgoingDirectory, "c.txt", "brand new file");
    commit(outgoing, "add file");
    write(outgoing, outgoingDirectory, "o.txt", "some file to be renamed");
    outgoing.rm().addFilepattern("d.txt").call();
    RevCommit targetCommit = commit(outgoing, "move/rename file");

    outgoing.checkout().setName("some_branch").setCreateBranch(true).setStartPoint(baseCommit).call();
    write(outgoing, outgoingDirectory, "x.txt", "bla bla");
    RevCommit otherBranchCommit = commit(outgoing, "other branch");

    Modifications modifications = outgoingModificationsCommand.getModifications(otherBranchCommit.getName(), targetCommit.getName());

    assertThat(modifications.getModifications())
      .hasSize(4)
      .extracting("class.simpleName")
      .contains("Modified") // File a.txt has been modified
      .contains("Removed") // File x.txt from the other branch is not present
      .contains("Added") // File c.txt has been created on the original branch
      .contains("Renamed") // File d.txt has been renamed on the original branch
    ;
  }

  void pushOutgoingAndPullIncoming() throws IOException {
    GitPushCommand cmd = new GitPushCommand(handler, new GitContext(outgoingDirectory, outgoingRepository, null, new GitConfig()));
    PushCommandRequest request = new PushCommandRequest();
    request.setRemoteRepository(incomingRepository);
    cmd.push(request);
    GitContext context = new GitContext(incomingDirectory, incomingRepository, null, new GitConfig());
    PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory = new PostReceiveRepositoryHookEventFactory(eventBus, eventFactory, context);
    GitPullCommand pullCommand = new GitPullCommand(
      handler,
      context,
      postReceiveRepositoryHookEventFactory,
      lfsLoader);
    PullCommandRequest pullRequest = new PullCommandRequest();
    pullRequest.setRemoteRepository(incomingRepository);
    pullCommand.pull(pullRequest);
  }

  Consumer<Modifications> assertRemovedFiles(String fileName) {
    return (modifications) -> {
      assertThat(modifications).isNotNull();
      assertThat(modifications.getAdded())
        .as("added files modifications")
        .asList()
        .isEmpty();
      assertThat(modifications.getModified())
        .as("modified files modifications")
        .asList()
        .isEmpty();
      assertThat(modifications.getRemoved())
        .as("removed files modifications")
        .asList()
        .hasSize(1)
        .extracting("path")
        .containsOnly(fileName);
    };
  }

  Consumer<Modifications> assertRenamedFiles(String fileName) {
    return (modifications) -> {
      assertThat(modifications).isNotNull();
      assertThat(modifications.getAdded())
        .as("added files modifications")
        .asList()
        .isEmpty();
      assertThat(modifications.getModified())
        .as("modified files modifications")
        .asList()
        .isEmpty();
      assertThat(modifications.getRemoved())
        .as("removed files modifications")
        .asList()
        .isEmpty();
      assertThat(modifications.getRenamed())
        .as("renamed files modifications")
        .asList()
        .hasSize(1)
        .extracting("newPath")
        .containsOnly(fileName);
    };
  }

  Consumer<Modifications> assertModifiedFiles(String file) {
    return (modifications) -> {
      assertThat(modifications).isNotNull();
      assertThat(modifications.getAdded())
        .as("added files modifications")
        .asList()
        .isEmpty();
      assertThat(modifications.getModified())
        .as("modified files modifications")
        .asList()
        .extracting("path")
        .hasSize(1)
        .containsOnly(file);
      assertThat(modifications.getRemoved())
        .as("removed files modifications")
        .asList()
        .isEmpty();
    };
  }

  Consumer<Modifications> assertAddedFiles(String file) {
    return (modifications) -> {
      assertThat(modifications).isNotNull();
      assertThat(modifications.getAdded())
        .as("added files modifications")
        .asList()
        .hasSize(1)
        .extracting("path")
        .containsOnly(file);
      assertThat(modifications.getModified())
        .as("modified files modifications")
        .asList()
        .isEmpty();
      assertThat(modifications.getRemoved())
        .as("removed files modifications")
        .asList()
        .isEmpty();
    };
  }
}
