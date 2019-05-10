package sonia.scm.repository.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryLocationResolver;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class PathBasedRepositoryLocationResolverTest {

  @Mock
  private SCMContextProvider contextProvider;

  @Mock
  private InitialRepositoryLocationResolver initialRepositoryLocationResolver;

  private PathBasedRepositoryLocationResolver resolver;

  @BeforeEach
  void beforeEach() {
    when(contextProvider.resolve(any(Path.class))).then((Answer<Path>) invocationOnMock -> invocationOnMock.getArgument(0));
    resolver = new PathBasedRepositoryLocationResolver(contextProvider, initialRepositoryLocationResolver);
  }

  // TODO implement tests
}
