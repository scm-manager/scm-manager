package sonia.scm.web.protocol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.PushStateDispatcher;
import sonia.scm.repository.DefaultRepositoryProvider;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.spi.HttpScmProtocol;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpProtocolServletTest {

  @Mock
  private RepositoryServiceFactory serviceFactory;

  @Mock
  private NamespaceAndNameFromPathExtractor extractor;

  @Mock
  private PushStateDispatcher dispatcher;

  @Mock
  private UserAgentParser userAgentParser;

  @InjectMocks
  private HttpProtocolServlet servlet;

  @Mock
  private RepositoryService repositoryService;

  @Mock
  private UserAgent userAgent;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpScmProtocol protocol;

  @Nested
  class Browser {

    @BeforeEach
    void prepareMocks() {
      when(userAgentParser.parse(request)).thenReturn(userAgent);
      when(userAgent.isBrowser()).thenReturn(true);
      when(request.getRequestURI()).thenReturn("uri");
    }

    @Test
    void shouldDispatchBrowserRequests() throws ServletException, IOException {
      when(userAgent.isBrowser()).thenReturn(true);
      when(request.getRequestURI()).thenReturn("uri");

      servlet.service(request, response);

      verify(dispatcher).dispatch(request, response, "uri");
    }

  }

  @Nested
  class ScmClient {

    @BeforeEach
    void prepareMocks() {
      when(userAgentParser.parse(request)).thenReturn(userAgent);
      when(userAgent.isBrowser()).thenReturn(false);
    }

    @Test
    void shouldHandleBadPaths() throws IOException, ServletException {
      when(request.getPathInfo()).thenReturn("/illegal");

      servlet.service(request, response);

      verify(response).setStatus(400);
    }

    @Test
    void shouldHandleNotExistingRepository() throws IOException, ServletException {
      when(request.getPathInfo()).thenReturn("/not/exists");

      NamespaceAndName repo = new NamespaceAndName("not", "exists");
      when(extractor.fromUri("/not/exists")).thenReturn(Optional.of(repo));
      when(serviceFactory.create(repo)).thenThrow(new NotFoundException("Test", "a"));

      servlet.service(request, response);

      verify(response).setStatus(404);
    }

    @Test
    void shouldDelegateToProvider() throws IOException, ServletException {
      NamespaceAndName repo = new NamespaceAndName("space", "name");
      when(extractor.fromUri("/space/name")).thenReturn(Optional.of(repo));
      when(serviceFactory.create(repo)).thenReturn(repositoryService);

      when(request.getPathInfo()).thenReturn("/space/name");
      Repository repository = RepositoryTestData.createHeartOfGold();
      when(repositoryService.getRepository()).thenReturn(repository);
      when(repositoryService.getProtocol(HttpScmProtocol.class)).thenReturn(protocol);

      servlet.service(request, response);

      verify(request).setAttribute(DefaultRepositoryProvider.ATTRIBUTE_NAME, repository);
      verify(protocol).serve(request, response, null);
      verify(repositoryService).close();
    }

  }
}
