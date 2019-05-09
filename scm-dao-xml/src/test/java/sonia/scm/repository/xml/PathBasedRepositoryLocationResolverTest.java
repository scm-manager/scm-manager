package sonia.scm.repository.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.PathBasedRepositoryDAO;
import sonia.scm.repository.RepositoryDAO;

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
  private PathBasedRepositoryDAO pathBasedRepositoryDAO;

  @Mock
  private RepositoryDAO repositoryDAO;

  @Mock
  private InitialRepositoryLocationResolver initialRepositoryLocationResolver;

  @BeforeEach
  void beforeEach() {
    when(contextProvider.resolve(any(Path.class))).then((Answer<Path>) invocationOnMock -> invocationOnMock.getArgument(0));
  }

  private PathBasedRepositoryLocationResolver createResolver(RepositoryDAO pathBasedRepositoryDAO) {
    return new PathBasedRepositoryLocationResolver(contextProvider, pathBasedRepositoryDAO, initialRepositoryLocationResolver);
  }

  @Test
  void shouldReturnPathFromDao() {
    Path repositoryPath = Paths.get("repos", "42");
    when(pathBasedRepositoryDAO.getPath("42")).thenReturn(repositoryPath);

    PathBasedRepositoryLocationResolver resolver = createResolver(pathBasedRepositoryDAO);
    Path path = resolver.forClass(Path.class).getLocation("42");

    assertThat(path).isSameAs(repositoryPath);
  }

  @Test
  void shouldReturnInitialPathIfDaoIsNotPathBased() {
    Path repositoryPath = Paths.get("r", "42");
    when(initialRepositoryLocationResolver.getPath("42")).thenReturn(repositoryPath);

    PathBasedRepositoryLocationResolver resolver = createResolver(repositoryDAO);
    Path path = resolver.forClass(Path.class).getLocation("42");

    assertThat(path).isSameAs(repositoryPath);
  }

}
