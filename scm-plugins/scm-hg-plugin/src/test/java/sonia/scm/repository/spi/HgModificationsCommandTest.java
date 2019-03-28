package sonia.scm.repository.spi;

import com.aragost.javahg.Changeset;
import com.aragost.javahg.commands.RemoveCommand;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.Modifications;

import java.io.File;
import java.util.function.Consumer;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class HgModificationsCommandTest extends IncomingOutgoingTestBase {


  private HgModificationsCommand outgoingModificationsCommand;

  @Before
  public void init() {
    HgCommandContext outgoingContext = new HgCommandContext(HgTestUtil.createHookManager(), handler, outgoingRepository, outgoingDirectory, null);
    outgoingModificationsCommand = new HgModificationsCommand(outgoingContext, outgoingRepository);
  }

  @Test
  public void shouldReadAddedFiles() throws Exception {
    String fileName = "a.txt";
    writeNewFile(outgoing, outgoingDirectory, fileName, "bal bla");
    Changeset changeset = commit(outgoing, "added a.txt");
    String revision = String.valueOf(changeset.getRevision());
    Consumer<Modifications> assertModifications = assertAddedFile(fileName);
    assertModifications.accept(outgoingModificationsCommand.getModifications(revision));
  }

  @Test
  public void shouldReadModifiedFiles() throws Exception {
    String fileName = "a.txt";
    writeNewFile(outgoing, outgoingDirectory, fileName, "bal bla");
    commit(outgoing, "added a.txt");
    writeNewFile(outgoing, outgoingDirectory, fileName, "new content");
    Changeset changeset = commit(outgoing, "modified a.txt");
    String revision = String.valueOf(changeset.getRevision());
    Consumer<Modifications> assertModifications = assertModifiedFiles(fileName);
    assertModifications.accept(outgoingModificationsCommand.getModifications(revision));
  }

  @Test
  public void shouldReadRemovedFiles() throws Exception {
    String fileName = "a.txt";
    writeNewFile(outgoing, outgoingDirectory, fileName, "bal bla");
    commit(outgoing, "added a.txt");
    File file = new File(outgoingDirectory, fileName);
    file.delete();
    RemoveCommand.on(outgoing).execute(file);
    Changeset changeset = commit(outgoing, "removed a.txt");
    String revision = String.valueOf(changeset.getRevision());
    Consumer<Modifications> assertModifications = assertRemovedFiles(fileName);
    assertModifications.accept(outgoingModificationsCommand.getModifications(revision));
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

  Consumer<Modifications> assertAddedFile(String addedFile) {
    return (modifications) -> {
      assertThat(modifications).isNotNull();
      assertThat(modifications.getAdded())
        .as("added files modifications")
        .hasSize(1)
        .containsOnly(addedFile);
      assertThat(modifications.getModified())
        .as("modified files modifications")
        .hasSize(0);
      assertThat(modifications.getRemoved())
        .as("removed files modifications")
        .hasSize(0);
    };
  }
}
