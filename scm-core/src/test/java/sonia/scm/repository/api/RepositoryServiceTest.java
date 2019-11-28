package sonia.scm.repository.api;

import org.junit.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.HttpScmProtocol;
import sonia.scm.repository.spi.RepositoryServiceProvider;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.util.IterableUtil.sizeOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class RepositoryServiceTest {

  private final RepositoryServiceProvider provider = mock(RepositoryServiceProvider.class);
  private final Repository repository = new Repository("", "git", "space", "repo");

  @Test
  public void shouldReturnMatchingProtocolsFromProvider() {
    RepositoryService repositoryService = new RepositoryService(null, provider, repository, null, Collections.singleton(new DummyScmProtocolProvider()), null);
    Stream<ScmProtocol> supportedProtocols = repositoryService.getSupportedProtocols();

    assertThat(sizeOf(supportedProtocols.collect(Collectors.toList()))).isEqualTo(1);
  }

  @Test
  public void shouldFindKnownProtocol() {
    RepositoryService repositoryService = new RepositoryService(null, provider, repository, null, Collections.singleton(new DummyScmProtocolProvider()), null);

    HttpScmProtocol protocol = repositoryService.getProtocol(HttpScmProtocol.class);

    assertThat(protocol).isNotNull();
  }

  @Test
  public void shouldFailForUnknownProtocol() {
    RepositoryService repositoryService = new RepositoryService(null, provider, repository, null, Collections.singleton(new DummyScmProtocolProvider()), null);

    assertThrows(IllegalArgumentException.class, () -> {
      repositoryService.getProtocol(UnknownScmProtocol.class);
    });
  }

  private static class DummyHttpProtocol extends HttpScmProtocol {
    public DummyHttpProtocol(Repository repository) {
      super(repository, "");
    }

    @Override
    public void serve(HttpServletRequest request, HttpServletResponse response, Repository repository, ServletConfig config) {
    }
  }

  private static class DummyScmProtocolProvider implements ScmProtocolProvider {
    @Override
    public String getType() {
      return "git";
    }

    @Override
    public ScmProtocol get(Repository repository) {
      return new DummyHttpProtocol(repository);
    }
  }

  private interface UnknownScmProtocol extends ScmProtocol {}
}
