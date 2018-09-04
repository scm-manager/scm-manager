package sonia.scm.repository.api;

import org.junit.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.HttpScmProtocol;
import sonia.scm.repository.spi.RepositoryServiceProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.util.IterableUtil.sizeOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepositoryServiceTest {

  private final RepositoryServiceProvider provider = mock(RepositoryServiceProvider.class);
  private final Repository repository = mock(Repository.class);

  @Test
  public void shouldReturnProtocolsFromProvider() {
    when(provider.getSupportedProtocols()).thenReturn(Collections.singleton(new DummyHttpProtocol()));

    RepositoryService repositoryService = new RepositoryService(null, provider, repository, null);
    Collection<ScmProtocol> supportedProtocols = repositoryService.getSupportedProtocols();

    assertThat(sizeOf(supportedProtocols)).isEqualTo(1);
  }

  @Test
  public void shouldFindProtocolFromProvider() {
    when(provider.getSupportedProtocols()).thenReturn(Collections.singleton(new DummyHttpProtocol()));

    RepositoryService repositoryService = new RepositoryService(null, provider, repository, null);
    HttpScmProtocol protocol = repositoryService.getProtocol(HttpScmProtocol.class);

    assertThat(protocol.getUrl(null)).isEqualTo("dummy");
  }

  @Test
  public void shouldFailForUnknownProtocol() {
    when(provider.getSupportedProtocols()).thenReturn(Collections.emptySet());

    RepositoryService repositoryService = new RepositoryService(null, provider, repository, null);

    assertThrows(IllegalArgumentException.class, () -> {
      repositoryService.getProtocol(HttpScmProtocol.class);
    });
  }

  private static class DummyHttpProtocol implements HttpScmProtocol {
    @Override
    public String getUrl(UriInfo uriInfo) {
      return "dummy";
    }

    @Override
    public void serve(HttpServletRequest request, HttpServletResponse response) {
    }
  }
}
