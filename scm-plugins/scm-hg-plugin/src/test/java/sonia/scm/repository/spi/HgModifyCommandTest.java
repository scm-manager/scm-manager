package sonia.scm.repository.spi;

import com.google.inject.util.Providers;
import org.junit.Test;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.Person;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.web.HgRepositoryEnvironmentBuilder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HgModifyCommandTest extends AbstractHgCommandTestBase {

  @Test
  public void shouldRemoveFiles() throws IOException {
    HgHookManager hookManager = mock(HgHookManager.class);
    when(hookManager.getChallenge()).thenReturn("CHALLENGE");
    when(hookManager.getCredentials()).thenReturn("SECRET:SECRET");
    when(hookManager.createUrl()).thenReturn("http://localhost");
    HgRepositoryEnvironmentBuilder environmentBuilder = new HgRepositoryEnvironmentBuilder(handler, hookManager);

    HgModifyCommand hgModifyCommand = new HgModifyCommand(handler, cmdContext, new SimpleHgWorkdirFactory(Providers.of(environmentBuilder), new WorkdirProvider()));
    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("a.txt"));
    request.setCommitMessage("this is great");
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));

    String result = hgModifyCommand.execute(request);

    assertThat(cmdContext.open().tip().getNode()).isEqualTo(result);
    cmdContext.close();
  }
}
