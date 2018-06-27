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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class UserRootResourceTest {

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

  private ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

  @Before
  public void prepareEnvironment() throws IOException, UserException {
    initMocks(this);
    User dummyUser = createDummyUser("Neo");
    when(userManager.getPage(any(), eq(0), eq(10))).thenReturn(new PageResult<>(singletonList(dummyUser), 1));
    when(userManager.get("Neo")).thenReturn(dummyUser);
    doNothing().when(userManager).create(userCaptor.capture());
    doNothing().when(userManager).modify(userCaptor.capture());
    doNothing().when(userManager).delete(userCaptor.capture());

    UserCollectionToDtoMapper userCollectionToDtoMapper = new UserCollectionToDtoMapper(userToDtoMapper, uriInfoStore);
    UserCollectionResource userCollectionResource = new UserCollectionResource(userManager, dtoToUserMapper,
      userCollectionToDtoMapper);
    UserResource userResource = new UserResource(dtoToUserMapper, userToDtoMapper, userManager);
    UserRootResource userRootResource = new UserRootResource(MockProvider.of(userCollectionResource),
                                                             MockProvider.of(userResource));

    dispatcher.getRegistry().addSingletonResource(userRootResource);
    when(uriInfo.getBaseUri()).thenReturn(URI.create("/"));
    when(uriInfoStore.get()).thenReturn(uriInfo);
  }

  @Test
  public void shouldCreateFullResponseForAdmin() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2 + "Neo");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"Neo\""));
    assertTrue(response.getContentAsString().contains("\"password\":\"__dummypassword__\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/users/Neo\"}"));
    assertTrue(response.getContentAsString().contains("\"delete\":{\"href\":\"/v2/users/Neo\"}"));
  }

  @Test
  @SubjectAware(username = "unpriv")
  public void shouldCreateLimitedResponseForSimpleUser() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"Neo\""));
    assertTrue(response.getContentAsString().contains("\"password\":\"__dummypassword__\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/users/Neo\"}"));
    assertFalse(response.getContentAsString().contains("\"delete\":{\"href\":\"/v2/users/Neo\"}"));
  }

  @Test
  public void shouldCreateNewUserWithEncryptedPassword() throws URISyntaxException, IOException, UserException {
    URL url = Resources.getResource("sonia/scm/api/v2/user-test-create.json");
    byte[] userJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + UserRootResource.USERS_PATH_V2)
      .contentType(VndMediaType.USER)
      .content(userJson);
    MockHttpResponse response = new MockHttpResponse();
    when(passwordService.encryptPassword("pwd123")).thenReturn("encrypted123");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
    verify(userManager).create(any(User.class));
    User createdUser = userCaptor.getValue();
    assertEquals("encrypted123", createdUser.getPassword());
  }

  @Test
  public void shouldUpdateChangedUserWithEncryptedPassword() throws URISyntaxException, IOException, UserException {
    URL url = Resources.getResource("sonia/scm/api/v2/user-test-update.json");
    byte[] userJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .put("/" + UserRootResource.USERS_PATH_V2 + "Neo")
      .contentType(VndMediaType.USER)
      .content(userJson);
    MockHttpResponse response = new MockHttpResponse();
    when(passwordService.encryptPassword("pwd123")).thenReturn("encrypted123");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    verify(userManager).modify(any(User.class));
    User updatedUser = userCaptor.getValue();
    assertEquals("encrypted123", updatedUser.getPassword());
  }

  @Test
  public void shouldFailForMissingContent() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .post("/" + UserRootResource.USERS_PATH_V2)
      .contentType(VndMediaType.USER)
      .content(new byte[] {});
    MockHttpResponse response = new MockHttpResponse();
    when(passwordService.encryptPassword("pwd123")).thenReturn("encrypted123");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
  }

  @Test
  public void shouldGetNotFoundForNotExistentUser() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2 + "nosuchuser");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldDeleteUser() throws URISyntaxException, IOException, UserException {
    MockHttpRequest request = MockHttpRequest.delete("/" + UserRootResource.USERS_PATH_V2 + "Neo");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(userManager).delete(any(User.class));
    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
  }

  @Test
  public void shouldFailUpdateForDifferentIds() throws IOException, URISyntaxException, UserException {
    URL url = Resources.getResource("sonia/scm/api/v2/user-test-update.json");
    byte[] userJson = Resources.toByteArray(url);
    when(userManager.get("Other")).thenReturn(createDummyUser("Other"));

    MockHttpRequest request = MockHttpRequest
      .put("/" + UserRootResource.USERS_PATH_V2 + "Other")
      .contentType(VndMediaType.USER)
      .content(userJson);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    verify(userManager, never()).modify(any(User.class));
  }

  @Test
  public void shouldFailUpdateForUnknownEntity() throws IOException, URISyntaxException, UserException {
    URL url = Resources.getResource("sonia/scm/api/v2/user-test-update.json");
    byte[] userJson = Resources.toByteArray(url);
    when(userManager.get("Neo")).thenReturn(null);

    MockHttpRequest request = MockHttpRequest
      .put("/" + UserRootResource.USERS_PATH_V2 + "Neo")
      .contentType(VndMediaType.USER)
      .content(userJson);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    verify(userManager, never()).modify(any(User.class));
  }

  private User createDummyUser(String name) {
    User user = new User();
    user.setName(name);
    user.setPassword("redpill");
    user.setCreationDate(System.currentTimeMillis());
    return user;
  }
}
