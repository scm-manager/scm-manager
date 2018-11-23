package sonia.scm.repository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.SCMContextProvider;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InitialRepositoryLocationResolverTest {

  @Mock
  private SCMContextProvider context;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void init() throws IOException {
    when(context.getBaseDirectory()).thenReturn(temporaryFolder.newFolder());
  }

  @Test
  public void shouldComputeInitialDirectory() {
    InitialRepositoryLocationResolver resolver = new InitialRepositoryLocationResolver(context);
    Repository repository = new Repository();
    repository.setId("ABC");
    File directory = resolver.getDefaultDirectory(repository);

    assertThat(directory).isEqualTo(new File(context.getBaseDirectory(), "repositories/ABC"));
  }
}
