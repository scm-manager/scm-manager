package sonia.scm.repository.spi;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.repository.Modifications;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class GitModificationsCommandTest extends AbstractRemoteCommandTestBase {

  private GitModificationsCommand incomingModificationsCommand;
  private GitModificationsCommand outgoingModificationsCommand;

  @Before
  public void init() {
    incomingModificationsCommand = new GitModificationsCommand(new GitContext(incomingDirectory), incomingRepository);
    outgoingModificationsCommand = new GitModificationsCommand(new GitContext(outgoingDirectory), outgoingRepository);
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
    outgoing.add().setUpdate(true).addFilepattern(".").call();
    RevCommit removedFileCommit = commit(outgoing, "remove file");
    String revision = removedFileCommit.getName();
    Consumer<Modifications> assertModifications = assertRemovedFiles(fileName);
    pushOutgoingAndPullIncoming();
    assertModifications.accept(incomingModificationsCommand.getModifications(revision));
    assertModifications.accept(outgoingModificationsCommand.getModifications(revision));
  }

  void pushOutgoingAndPullIncoming() throws IOException {
    GitPushCommand cmd = new GitPushCommand(handler, new GitContext(outgoingDirectory),
      outgoingRepository);
    PushCommandRequest request = new PushCommandRequest();
    request.setRemoteRepository(incomingRepository);
    cmd.push(request);
    GitPullCommand pullCommand = new GitPullCommand(handler, new GitContext(incomingDirectory),
      incomingRepository);
    PullCommandRequest pullRequest = new PullCommandRequest();
    pullRequest.setRemoteRepository(incomingRepository);
    pullCommand.pull(pullRequest);
  }

  Consumer<Modifications> assertRemovedFiles(String fileName) {
    return (modifications) -> {
      assertThat(modifications).isNotNull();
      assertThat(modifications.getAdded())
        .as("added files modifications")
        .hasSize(0);
      assertThat(modifications.getModified())
        .as("modified files modifications")
        .hasSize(0);
      assertThat(modifications.getRemoved())
        .as("removed files modifications")
        .hasSize(1)
        .containsOnly(fileName);
    };
  }


  Consumer<Modifications> assertModifiedFiles(String file) {
    return (modifications) -> {
      assertThat(modifications).isNotNull();
      assertThat(modifications.getAdded())
        .as("added files modifications")
        .hasSize(0);
      assertThat(modifications.getModified())
        .as("modified files modifications")
        .hasSize(1)
        .containsOnly(file);
      assertThat(modifications.getRemoved())
        .as("removed files modifications")
        .hasSize(0);
    };
  }

  Consumer<Modifications> assertAddedFiles(String file) {
    return (modifications) -> {
      assertThat(modifications).isNotNull();
      assertThat(modifications.getAdded())
        .as("added files modifications")
        .hasSize(1)
        .containsOnly(file);
      assertThat(modifications.getModified())
        .as("modified files modifications")
        .hasSize(0);
      assertThat(modifications.getRemoved())
        .as("removed files modifications")
        .hasSize(0);
    };
  }
}
