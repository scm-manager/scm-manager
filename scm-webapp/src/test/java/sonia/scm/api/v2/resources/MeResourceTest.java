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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class MeResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();


  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("/"));
  @Mock
  private ScmPathInfo uriInfo;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;

  @Mock
  private UserManager userManager;

  @InjectMocks
  private UserToUserDtoMapperImpl userToDtoMapper;

  private ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

  @Before
  public void prepareEnvironment() throws Exception {
    initMocks(this);
    createDummyUser("trillian");
    when(userManager.create(userCaptor.capture())).thenAnswer(invocation -> invocation.getArguments()[0]);
    doNothing().when(userManager).modify(userCaptor.capture());
    doNothing().when(userManager).delete(userCaptor.capture());
    userToDtoMapper.setResourceLinks(resourceLinks);
    MeResource meResource = new MeResource(userToDtoMapper, userManager);
    dispatcher.getRegistry().addSingletonResource(meResource);
    when(uriInfo.getApiRestUri()).thenReturn(URI.create("/"));
    when(scmPathInfoStore.get()).thenReturn(uriInfo);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnCurrentlyAuthenticatedUser() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + MeResource.ME_PATH_V2);
    request.accept(VndMediaType.USER);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"trillian\""));
    assertTrue(response.getContentAsString().contains("\"password\":\"__dummypassword__\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/users/trillian\"}"));
    assertTrue(response.getContentAsString().contains("\"delete\":{\"href\":\"/v2/users/trillian\"}"));
  }

  private User createDummyUser(String name) {
    User user = new User();
    user.setName(name);
    user.setPassword("secret");
    user.setCreationDate(System.currentTimeMillis());
    when(userManager.get(name)).thenReturn(user);
    return user;
  }
}
