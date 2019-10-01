package sonia.scm.repository.spi;

import com.google.inject.util.Providers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.AlreadyExistsException;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.Person;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.web.HgRepositoryEnvironmentBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HgModifyCommandTest extends AbstractHgCommandTestBase {

  private HgModifyCommand hgModifyCommand;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void initHgModifyCommand() {
    HgHookManager hookManager = mock(HgHookManager.class);
    when(hookManager.getChallenge()).thenReturn("CHALLENGE");
    when(hookManager.getCredentials()).thenReturn("SECRET:SECRET");
    when(hookManager.createUrl()).thenReturn("http://localhost");
    HgRepositoryEnvironmentBuilder environmentBuilder = new HgRepositoryEnvironmentBuilder(handler, hookManager);
    hgModifyCommand = new HgModifyCommand(handler, cmdContext, new SimpleHgWorkdirFactory(Providers.of(environmentBuilder), new WorkdirProvider()));
  }

  @After
  public void closeRepository() throws IOException {
    cmdContext.close();
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

    File testFile = temporaryFolder.newFile("Answer.txt");

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

    File testFile = temporaryFolder.newFile("Answer.txt");
    new FileOutputStream(testFile).write(21);

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Answer.txt", testFile, false));
    request.setCommitMessage("I found the answer");
    request.setAuthor(new Person("Trillian Astra", "trillian@hitchhiker.com"));

    hgModifyCommand.execute(request);

    new FileOutputStream(testFile).write(42);
    ModifyCommandRequest request2 = new ModifyCommandRequest();
    request2.addRequest(new ModifyCommandRequest.CreateFileRequest("Answer.txt", testFile, true));
    request2.setCommitMessage(" Now i really found the answer");
    request2.setAuthor(new Person("Trillian Astra", "trillian@hitchhiker.com"));

    String changeSet2 = hgModifyCommand.execute(request2);

    assertThat(cmdContext.open().tip().getNode()).isEqualTo(changeSet2);
    assertThat(cmdContext.open().tip().getModifiedFiles().size()).isEqualTo(1);
    assertThat(cmdContext.open().tip().getModifiedFiles().get(0)).isEqualTo(testFile.getName());
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldThrowFileAlreadyExistsException() throws IOException {

    File testFile = temporaryFolder.newFile("Answer.txt");
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
}
