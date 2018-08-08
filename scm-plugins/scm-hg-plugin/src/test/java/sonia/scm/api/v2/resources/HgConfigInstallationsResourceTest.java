package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@SubjectAware(
  configuration = "classpath:sonia/scm/configuration/shiro.ini",
  password = "secret"
)
@RunWith(MockitoJUnitRunner.class)
public class HgConfigInstallationsResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  private final URI baseUri = URI.create("/");

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private UriInfoStore uriInfoStore;

  @InjectMocks
  private HgConfigInstallationsToDtoMapper mapper;

  @Mock
  private Provider<HgConfigInstallationsResource> resourceProvider;


  @Before
  public void prepareEnvironment() {
    HgConfigInstallationsResource resource = new HgConfigInstallationsResource(mapper);

    when(resourceProvider.get()).thenReturn(resource);
    dispatcher.getRegistry().addSingletonResource(
      new HgConfigResource(null, null, null, null,
                           null, resourceProvider));

    when(uriInfoStore.get().getBaseUri()).thenReturn(baseUri);
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetHgInstallations() throws Exception {
    MockHttpResponse response = get("hg");

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    String contentAsString = response.getContentAsString();
    assertThat(contentAsString).contains("{\"paths\":[");
    assertThat(contentAsString).contains("hg");
    assertThat(contentAsString).doesNotContain("python");

    assertThat(contentAsString).contains("\"self\":{\"href\":\"/v2/config/hg/installations/hg");
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldNotGetHgInstallationsWhenNotAuthorized() throws Exception {
    thrown.expectMessage("Subject does not have permission [configuration:read:hg]");

    get("hg");
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetPythonInstallations() throws Exception {
    MockHttpResponse response = get("python");

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    String contentAsString = response.getContentAsString();
    assertThat(contentAsString).contains("{\"paths\":[");
    assertThat(contentAsString).contains("python");

    assertThat(contentAsString).contains("\"self\":{\"href\":\"/v2/config/hg/installations/python");
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldNotGetPythonInstallationsWhenNotAuthorized() throws Exception {
    thrown.expectMessage("Subject does not have permission [configuration:read:hg]");

    get("python");
  }

  private MockHttpResponse get(String path) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + HgConfigResource.HG_CONFIG_PATH_V2 + "/installations/" + path);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }
}
