package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.io.Resources;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
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
import sonia.scm.PageResult;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
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
  private UriInfo uriInfo;
  @Mock
  private UriInfoStore uriInfoStore;

  @Mock
  private UserManager userManager;

  @InjectMocks
  private UserToUserDtoMapperImpl userToDtoMapper;

  private ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

  @Before
  public void prepareEnvironment() throws IOException, UserException {
    initMocks(this);
    createDummyUser("trillian");
    when(userManager.create(userCaptor.capture())).thenAnswer(invocation -> invocation.getArguments()[0]);
    doNothing().when(userManager).modify(userCaptor.capture());
    doNothing().when(userManager).delete(userCaptor.capture());
    userToDtoMapper.setResourceLinks(resourceLinks);
    MeResource meResource = new MeResource(userToDtoMapper, userManager);
    dispatcher.getRegistry().addSingletonResource(meResource);
    when(uriInfo.getBaseUri()).thenReturn(URI.create("/"));
    when(uriInfoStore.get()).thenReturn(uriInfo);
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
