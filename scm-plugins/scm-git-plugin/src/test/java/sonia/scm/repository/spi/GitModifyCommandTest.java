package sonia.scm.repository.spi;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.repository.util.WorkdirProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GitModifyCommandTest extends AbstractGitCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();

  @Test
  public void shouldCreateNewFile() throws IOException {
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = new GitModifyCommand(createContext(), repository, new SimpleGitWorkdirFactory(new WorkdirProvider()));

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setBranch("master");
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new/file", newFile));
    command.execute(request);
  }
}
