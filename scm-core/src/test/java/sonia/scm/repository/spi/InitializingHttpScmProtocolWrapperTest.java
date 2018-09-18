package sonia.scm.repository.spi;

import com.google.inject.ProvisionException;
import com.google.inject.util.Providers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
import sonia.scm.api.v2.resources.ScmPathInfo;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;

import javax.inject.Provider;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class InitializingHttpScmProtocolWrapperTest {

  private static final Repository REPOSITORY = new Repository("", "git", "space", "name");

  @Mock
  private ScmProviderHttpServlet delegateServlet;
  @Mock
  private ScmPathInfoStore pathInfoStore;
  @Mock
  private ScmConfiguration scmConfiguration;
  private Provider<ScmPathInfoStore> pathInfoStoreProvider;

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private ServletConfig servletConfig;

  private InitializingHttpScmProtocolWrapper wrapper;

  @Before
  public void init() {
    initMocks(this);
    pathInfoStoreProvider = mock(Provider.class);
    when(pathInfoStoreProvider.get()).thenReturn(pathInfoStore);

    wrapper = new InitializingHttpScmProtocolWrapper(Providers.of(this.delegateServlet), pathInfoStoreProvider, scmConfiguration) {
      @Override
      public String getType() {
        return "git";
      }
    };
    when(scmConfiguration.getBaseUrl()).thenReturn("http://example.com/scm");
  }

  @Test
  public void shouldUsePathFromPathInfo() {
    mockSetPathInfo();

    HttpScmProtocol httpScmProtocol = wrapper.get(REPOSITORY);

    assertEquals("http://example.com/scm/repo/space/name", httpScmProtocol.getUrl());
  }

  @Test
  public void shouldUseConfigurationWhenPathInfoNotSet() {
    HttpScmProtocol httpScmProtocol = wrapper.get(REPOSITORY);

    assertEquals("http://example.com/scm/repo/space/name", httpScmProtocol.getUrl());
  }

  @Test
  public void shouldUseConfigurationWhenNotInRequestScope() {
    when(pathInfoStoreProvider.get()).thenThrow(new ProvisionException("test"));

    HttpScmProtocol httpScmProtocol = wrapper.get(REPOSITORY);

    assertEquals("http://example.com/scm/repo/space/name", httpScmProtocol.getUrl());
  }

  @Test
  public void shouldInitializeAndDelegateRequestThroughFilter() throws ServletException, IOException {
    HttpScmProtocol httpScmProtocol = wrapper.get(REPOSITORY);

    httpScmProtocol.serve(request, response, servletConfig);

    verify(delegateServlet).init(servletConfig);
    verify(delegateServlet).service(request, response, REPOSITORY);
  }

  @Test
  public void shouldInitializeOnlyOnce() throws ServletException, IOException {
    HttpScmProtocol httpScmProtocol = wrapper.get(REPOSITORY);

    httpScmProtocol.serve(request, response, servletConfig);
    httpScmProtocol.serve(request, response, servletConfig);

    verify(delegateServlet, times(1)).init(servletConfig);
    verify(delegateServlet, times(2)).service(request, response, REPOSITORY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailForIllegalScmType() {
    HttpScmProtocol httpScmProtocol = wrapper.get(new Repository("", "other", "space", "name"));
  }

  private OngoingStubbing<ScmPathInfo> mockSetPathInfo() {
    return when(pathInfoStore.get()).thenReturn(() -> URI.create("http://example.com/scm/api/rest/"));
  }

}
