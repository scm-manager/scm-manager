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

import com.google.inject.util.Providers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.AlreadyExistsException;
import sonia.scm.NoChangesMadeException;
import sonia.scm.NotFoundException;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.Person;
import sonia.scm.repository.util.NoneCachingWorkdirProvider;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.web.HgRepositoryEnvironmentBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class HgModifyCommandTest extends AbstractHgCommandTestBase {

  private HgModifyCommand hgModifyCommand;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void initHgModifyCommand() {
    HgHookManager hookManager = HgTestUtil.createHookManager();
    HgRepositoryEnvironmentBuilder environmentBuilder = new HgRepositoryEnvironmentBuilder(handler, hookManager);
    SimpleHgWorkdirFactory workdirFactory = new SimpleHgWorkdirFactory(Providers.of(environmentBuilder), new NoneCachingWorkdirProvider(new WorkdirProvider())) {
      @Override
      public void configure(com.aragost.javahg.commands.PullCommand pullCommand) {
        // we do not want to configure http hooks in this unit test
      }
    };
    hgModifyCommand = new HgModifyCommand(cmdContext, workdirFactory
    );
  }

  @Test
  public void shouldRemoveFiles() {
    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("a.txt"));
    request.setCommitMessage("this is great");
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));

    String result = hgModifyCommand.execute(request);

    assertThat(cmdContext.open().tip().getNode()).isEqualTo(result);
  }

  @Test
  public void shouldCreateFilesWithoutOverwrite() throws IOException {
    File testFile = temporaryFolder.newFile();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Answer.txt", testFile, false));
    request.setCommitMessage("I found the answer");
    request.setAuthor(new Person("Trillian Astra", "trillian@hitchhiker.com"));

    String changeSet = hgModifyCommand.execute(request);

    assertThat(cmdContext.open().tip().getNode()).isEqualTo(changeSet);
    assertThat(cmdContext.open().tip().getAddedFiles().size()).isEqualTo(1);
  }

  @Test
  public void shouldOverwriteExistingFiles() throws IOException {
    File testFile = temporaryFolder.newFile();

    new FileOutputStream(testFile).write(42);
    ModifyCommandRequest request2 = new ModifyCommandRequest();
    request2.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt", testFile, true));
    request2.setCommitMessage(" Now i really found the answer");
    request2.setAuthor(new Person("Trillian Astra", "trillian@hitchhiker.com"));

    String changeSet2 = hgModifyCommand.execute(request2);

    assertThat(cmdContext.open().tip().getNode()).isEqualTo(changeSet2);
    assertThat(cmdContext.open().tip().getModifiedFiles().size()).isEqualTo(1);
    assertThat(cmdContext.open().tip().getModifiedFiles().get(0)).isEqualTo("a.txt");
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldThrowFileAlreadyExistsException() throws IOException {

    File testFile = temporaryFolder.newFile();
    new FileOutputStream(testFile).write(21);

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Answer.txt", testFile, false));
    request.setCommitMessage("I found the answer");
    request.setAuthor(new Person("Trillian Astra", "trillian@hitchhiker.com"));

    hgModifyCommand.execute(request);

    new FileOutputStream(testFile).write(42);
    ModifyCommandRequest request2 = new ModifyCommandRequest();
    request2.addRequest(new ModifyCommandRequest.CreateFileRequest("Answer.txt", testFile, false));
    request2.setCommitMessage(" Now i really found the answer");
    request2.setAuthor(new Person("Trillian Astra", "trillian@hitchhiker.com"));

    hgModifyCommand.execute(request2);
  }

  @Test
  public void shouldModifyExistingFile() throws IOException {
    File testFile = temporaryFolder.newFile("a.txt");

    new FileOutputStream(testFile).write(42);
    ModifyCommandRequest request2 = new ModifyCommandRequest();
    request2.addRequest(new ModifyCommandRequest.ModifyFileRequest("a.txt", testFile));
    request2.setCommitMessage(" Now i really found the answer");
    request2.setAuthor(new Person("Trillian Astra", "trillian@hitchhiker.com"));

    String changeSet2 = hgModifyCommand.execute(request2);

    assertThat(cmdContext.open().tip().getNode()).isEqualTo(changeSet2);
    assertThat(cmdContext.open().tip().getModifiedFiles().size()).isEqualTo(1);
    assertThat(cmdContext.open().tip().getModifiedFiles().get(0)).isEqualTo(testFile.getName());
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowNotFoundExceptionIfFileDoesNotExist() throws IOException {
    File testFile = temporaryFolder.newFile("Answer.txt");

    new FileOutputStream(testFile).write(42);
    ModifyCommandRequest request2 = new ModifyCommandRequest();
    request2.addRequest(new ModifyCommandRequest.ModifyFileRequest("Answer.txt", testFile));
    request2.setCommitMessage(" Now i really found the answer");
    request2.setAuthor(new Person("Trillian Astra", "trillian@hitchhiker.com"));

    hgModifyCommand.execute(request2);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNPEIfAuthorIsMissing() throws IOException {
    File testFile = temporaryFolder.newFile();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Answer.txt", testFile, false));
    request.setCommitMessage("I found the answer");
    hgModifyCommand.execute(request);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNPEIfCommitMessageIsMissing() throws IOException {
    File testFile = temporaryFolder.newFile();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Answer.txt", testFile, false));
    request.setAuthor(new Person("Trillian Astra", "trillian@hitchhiker.com"));
    hgModifyCommand.execute(request);
  }

  @Test(expected = NoChangesMadeException.class)
  public void shouldThrowNoChangesMadeExceptionIfEmptyLocalChangesetAfterRequest() {
    hgModifyCommand.execute(new ModifyCommandRequest());
  }
}
