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

import org.javahg.Changeset;
import org.javahg.commands.BranchCommand;
import org.javahg.commands.CopyCommand;
import org.javahg.commands.RemoveCommand;
import org.javahg.commands.RenameCommand;
import org.javahg.commands.UpdateCommand;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.repository.HgConfigResolver;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.client.spi.CheckoutCommand;

import java.io.File;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class HgModificationsCommandTest extends IncomingOutgoingTestBase {

  private HgModificationsCommand outgoingModificationsCommand;

  @Before
  public void init() {
    HgConfigResolver configResolver = new HgConfigResolver(handler);
    HgCommandContext outgoingContext = new HgCommandContext(configResolver, HgTestUtil.createFactory(handler, outgoingDirectory), outgoingRepository);
    outgoingModificationsCommand = new HgModificationsCommand(outgoingContext);
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

  @Test
  public void shouldReadRenamedFiles() throws Exception {
    String oldFileName = "a.txt";
    String newFileName = "b.txt";
    writeNewFile(outgoing, outgoingDirectory, oldFileName, "bal bla");
    commit(outgoing, "added a.txt");
    RenameCommand.on(outgoing).execute(oldFileName, newFileName);
    Changeset changeset = commit(outgoing, "rename a.txt to b.txt");
    String revision = String.valueOf(changeset.getRevision());
    Consumer<Modifications> assertModifications = assertRenamedFiles(oldFileName, newFileName);
    assertModifications.accept(outgoingModificationsCommand.getModifications(revision));
  }

  @Test
  public void shouldReadCopiedFiles() throws Exception {
    String srcFileName = "a.txt";
    String newFileName = "b.txt";
    writeNewFile(outgoing, outgoingDirectory, srcFileName, "bal bla");
    commit(outgoing, "added a.txt");
    CopyCommand.on(outgoing).execute(srcFileName, newFileName);
    Changeset changeset = commit(outgoing, "copy a.txt to b.txt");
    String revision = String.valueOf(changeset.getRevision());
    Consumer<Modifications> assertModifications = assertCopiedFiles(srcFileName, newFileName);
    assertModifications.accept(outgoingModificationsCommand.getModifications(revision));
  }

  @Test
  public void shouldFindModificationsBetweenRevisions() throws Exception {
    writeNewFile(outgoing, outgoingDirectory, "a.txt", "bla bla");
    writeNewFile(outgoing, outgoingDirectory, "42.txt", "the answer to life and everything");
    writeNewFile(outgoing, outgoingDirectory, "SpaceX.txt", "Going to infinity and beyond");
    commit(outgoing, "add files");
    BranchCommand.on(outgoing).set("some_branch");
    writeNewFile(outgoing, outgoingDirectory, "x.txt", "bla bla");
    Changeset otherBranchCommit = commit(outgoing, "other branch");

    UpdateCommand.on(outgoing).rev("default").execute();
    writeNewFile(outgoing, outgoingDirectory, "a.txt", "modified content");
    commit(outgoing, "modify file");
    RenameCommand.on(outgoing).execute("42.txt", "7x6.txt");
    commit(outgoing, "rename file");
    CopyCommand.on(outgoing).execute("SpaceX.txt", "Virgin.txt");
    commit(outgoing, "copy file");
    writeNewFile(outgoing, outgoingDirectory, "c.txt", "brand new file");
    Changeset targetChangeset = commit(outgoing, "add file");

    Modifications modifications = outgoingModificationsCommand.getModifications(otherBranchCommit.getNode(), targetChangeset.getNode());

    assertThat(modifications.getModifications())
      .hasSize(6)
      .extracting("class.simpleName")
      .contains("Modified") // File a.txt has been modified
      .contains("Removed") // File x.txt from the other branch is not present and 42.txt has been removed (via rename)
      .contains("Added") // File c.txt, Virgin.txt, and 7x6.txt have been created (or copied or renamed) on the original branch
    ;
  }

  Consumer<Modifications> assertRemovedFiles(String fileName) {
    return (modifications) -> {
      assertThat(modifications).isNotNull();
      assertThat(modifications.getAdded())
        .as("added files modifications")
        .isEmpty();
      assertThat(modifications.getModified())
        .as("modified files modifications")
        .isEmpty();
      assertThat(modifications.getRemoved())
        .as("removed files modifications")
        .hasSize(1)
        .extracting("path")
        .containsOnly(fileName);
      assertThat(modifications.getRenamed())
        .as("renamed files modifications")
        .isEmpty();
    };
  }

  Consumer<Modifications> assertRenamedFiles(String oldFileName, String newFileName) {
    return (modifications) -> {
      assertThat(modifications).isNotNull();
      assertThat(modifications.getAdded())
        .as("added files modifications")
        .isEmpty();
      assertThat(modifications.getModified())
        .as("modified files modifications")
        .isEmpty();
      assertThat(modifications.getRemoved())
        .as("removed files modifications")
        .isEmpty();
      assertThat(modifications.getRenamed())
        .as("renamed files modifications")
        .hasSize(1)
        .extracting("oldPath")
        .containsOnly(oldFileName);
      assertThat(modifications.getRenamed())
        .as("renamed files modifications")
        .hasSize(1)
        .extracting("newPath")
        .containsOnly(newFileName);
    };
  }

  Consumer<Modifications> assertCopiedFiles(String srcFileName, String newFileName) {
    return (modifications) -> {
      assertThat(modifications).isNotNull();
      assertThat(modifications.getAdded())
        .as("added files modifications")
        .isEmpty();
      assertThat(modifications.getModified())
        .as("modified files modifications")
        .isEmpty();
      assertThat(modifications.getRemoved())
        .as("removed files modifications")
        .isEmpty();
      assertThat(modifications.getRenamed())
        .as("renamed files modifications")
        .isEmpty();
      assertThat(modifications.getCopied())
        .as("copied files modifications")
        .hasSize(1)
        .extracting("sourcePath")
        .containsOnly(srcFileName);
      assertThat(modifications.getCopied())
        .as("copied files modifications")
        .hasSize(1)
        .extracting("targetPath")
        .containsOnly(newFileName);
    };
  }

  Consumer<Modifications> assertModifiedFiles(String file) {
    return (modifications) -> {
      assertThat(modifications).isNotNull();
      assertThat(modifications.getAdded())
        .as("added files modifications")
        .isEmpty();
      assertThat(modifications.getModified())
        .as("modified files modifications")
        .hasSize(1)
        .extracting("path")
        .containsOnly(file);
      assertThat(modifications.getRemoved())
        .as("removed files modifications")
        .isEmpty();
      assertThat(modifications.getRenamed())
        .as("renamed files modifications")
        .isEmpty();
    };
  }

  Consumer<Modifications> assertAddedFile(String addedFile) {
    return (modifications) -> {
      assertThat(modifications).isNotNull();
      assertThat(modifications.getAdded())
        .as("added files modifications")
        .hasSize(1)
        .extracting("path")
        .containsOnly(addedFile);
      assertThat(modifications.getModified())
        .as("modified files modifications")
        .isEmpty();
      assertThat(modifications.getRemoved())
        .as("removed files modifications")
        .isEmpty();
      assertThat(modifications.getRenamed())
        .as("renamed files modifications")
        .isEmpty();
    };
  }
}
