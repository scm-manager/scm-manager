package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.io.Resources;
import org.apache.shiro.authc.credential.PasswordService;
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
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class UserV2ResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  @Mock
  private UriInfo uriInfo;
  @Mock
  private UriInfoStore uriInfoStore;

  @Mock
  private PasswordService passwordService;
  @Mock
  private UserManager userManager;
  @InjectMocks
  private UserDtoToUserMapperImpl dtoToUserMapper;
  @InjectMocks
  private UserToUserDtoMapperImpl userToDtoMapper;

  private UserCollectionToDtoMapper userCollectionToDtoMapper;

  private ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

  @Before
  public void prepareEnvironment() throws IOException, UserException {
    initMocks(this);
    User dummyUser = createDummyUser();
    when(userManager.getPage(any(), eq(0), eq(10))).thenReturn(new PageResult<>(Collections.singletonList(dummyUser), true));
    when(userManager.get("Neo")).thenReturn(dummyUser);
    doNothing().when(userManager).create(userCaptor.capture());

    userCollectionToDtoMapper = new UserCollectionToDtoMapper(userToDtoMapper, uriInfoStore);
    UserCollectionResource userCollectionResource = new UserCollectionResource(userManager, dtoToUserMapper, userToDtoMapper, userCollectionToDtoMapper);
    UserSubResource userSubResource = new UserSubResource(dtoToUserMapper, userToDtoMapper, userManager);
    UserV2Resource userV2Resource = new UserV2Resource(userCollectionResource, userSubResource);

    dispatcher.getRegistry().addSingletonResource(userV2Resource);
    when(uriInfo.getBaseUri()).thenReturn(URI.create("/"));
    when(uriInfoStore.get()).thenReturn(uriInfo);
  }

  @Test
  public void shouldCreateFullResponseForAdmin() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + UserV2Resource.USERS_PATH_V2 + "Neo");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    System.out.println(response.getContentAsString());
    assertTrue(response.getContentAsString().contains("\"name\":\"Neo\""));
    assertTrue(response.getContentAsString().contains("\"password\":\"__dummypassword__\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/users/Neo\"}"));
    assertTrue(response.getContentAsString().contains("\"delete\":{\"href\":\"/v2/users/Neo\"}"));
  }

  @Test
  @SubjectAware(username = "unpriv")
  public void shouldCreateLimitedResponseForSimpleUser() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + UserV2Resource.USERS_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"Neo\""));
    assertTrue(response.getContentAsString().contains("\"password\":\"__dummypassword__\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/users/Neo\"}"));
    assertFalse(response.getContentAsString().contains("\"delete\":{\"href\":\"/v2/users/Neo\"}"));
  }

  @Test
  @SubjectAware(username = "unpriv")
  public void shouldNotGetSingleUserForSimpleUser() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + UserV2Resource.USERS_PATH_V2 + "Neo");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  public void shouldCreateNewUserWithEncryptedPassword() throws URISyntaxException, IOException {
    URL url = Resources.getResource("sonia/scm/api/v2/user-test-create.json");
    byte[] userJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + UserV2Resource.USERS_PATH_V2)
      .contentType(VndMediaType.USER)
      .content(userJson);
    MockHttpResponse response = new MockHttpResponse();
    when(passwordService.encryptPassword("pwd123")).thenReturn("encrypted123");

    dispatcher.invoke(request, response);

    assertEquals(201, response.getStatus());
    User createdUser = userCaptor.getValue();
    assertNotNull(createdUser);
    assertEquals("encrypted123", createdUser.getPassword());
  }

  @Test
  public void shouldFailForMissingContent() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .post("/" + UserV2Resource.USERS_PATH_V2)
      .contentType(VndMediaType.USER)
      .content(new byte[] {});
    MockHttpResponse response = new MockHttpResponse();
    when(passwordService.encryptPassword("pwd123")).thenReturn("encrypted123");

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  private User createDummyUser() {
    User user = new User();
    user.setName("Neo");
    user.setPassword("redpill");
    user.setCreationDate(System.currentTimeMillis());
    return user;
  }
}
