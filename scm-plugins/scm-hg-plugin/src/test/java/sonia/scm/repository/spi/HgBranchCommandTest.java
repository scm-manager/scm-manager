package sonia.scm.repository.spi;

import com.google.inject.util.Providers;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import sonia.scm.repository.Branch;
import sonia.scm.repository.HgHookManager;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.web.HgRepositoryEnvironmentBuilder;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HgBranchCommandTest extends AbstractHgCommandTestBase {
  @Test
  public void shouldCreateBranch() {
    Assertions.assertThat(readBranches()).filteredOn(b -> b.getName().equals("new_branch")).isEmpty();

    HgHookManager hookManager = mock(HgHookManager.class);
    when(hookManager.getChallenge()).thenReturn("1");
    when(hookManager.createUrl()).thenReturn("http://example.com/");
    AccessTokenBuilderFactory tokenBuilderFactory = mock(AccessTokenBuilderFactory.class);
    AccessTokenBuilder tokenBuilder = mock(AccessTokenBuilder.class);
    when(tokenBuilderFactory.create()).thenReturn(tokenBuilder);
    AccessToken accessToken = mock(AccessToken.class);
    when(tokenBuilder.build()).thenReturn(accessToken);
    when(accessToken.compact()).thenReturn("");

    HgRepositoryEnvironmentBuilder hgRepositoryEnvironmentBuilder =
      new HgRepositoryEnvironmentBuilder(handler, hookManager, tokenBuilderFactory);

    SimpleHgWorkdirFactory workdirFactory = new SimpleHgWorkdirFactory(Providers.of(hgRepositoryEnvironmentBuilder), pc -> {});

    new HgBranchCommand(cmdContext, repository, workdirFactory).branch("new_branch");

    Assertions.assertThat(readBranches()).filteredOn(b -> b.getName().equals("new_branch")).isNotEmpty();
  }

  private List<Branch> readBranches() {
    return new HgBranchesCommand(cmdContext, repository).getBranches();
  }
}
