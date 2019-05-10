package sonia.scm.repository.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class PathBasedRepositoryLocationResolverTest {

  @Mock
  private SCMContextProvider contextProvider;

  @Mock
  private RepositoryDAO repositoryDAO;

  @Mock
  private InitialRepositoryLocationResolver initialRepositoryLocationResolver;

  @Mock
  private SCMContextProvider context;

  @BeforeEach
  void beforeEach() {
    when(contextProvider.resolve(any(Path.class))).then((Answer<Path>) invocationOnMock -> invocationOnMock.getArgument(0));
  }

  private PathBasedRepositoryLocationResolver createResolver(RepositoryDAO pathBasedRepositoryDAO) {
    return new PathBasedRepositoryLocationResolver(contextProvider, initialRepositoryLocationResolver, context);
  }

  // TODO implement tests
}
