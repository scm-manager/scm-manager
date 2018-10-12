package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.apache.shiro.authc.credential.PasswordService;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.VndMediaType;

import javax.lang.model.util.Types;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static sonia.scm.api.v2.resources.DispatcherMock.createDispatcher;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class MeResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private Dispatcher dispatcher;

  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("/"));
  @Mock
  private ScmPathInfo uriInfo;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;

  @Mock
  private UserManager userManager;

  @InjectMocks
  private MeToUserDtoMapperImpl userToDtoMapper;

  private ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

  @Mock
  private PasswordService passwordService;
  private User originalUser;

  @Before
  public void prepareEnvironment() throws Exception {
    initMocks(this);
    originalUser = createDummyUser("trillian");
    when(userManager.create(userCaptor.capture())).thenAnswer(invocation -> invocation.getArguments()[0]);
    doNothing().when(userManager).modify(userCaptor.capture());
    doNothing().when(userManager).delete(userCaptor.capture());
    when(userManager.isTypeDefault(userCaptor.capture())).thenCallRealMethod();
    when(userManager.getChangePasswordChecker()).thenCallRealMethod();
    when(userManager.getDefaultType()).thenReturn("xml");
    MeResource meResource = new MeResource(userToDtoMapper, userManager, passwordService);
    when(uriInfo.getApiRestUri()).thenReturn(URI.create("/"));
    when(scmPathInfoStore.get()).thenReturn(uriInfo);
    dispatcher = createDispatcher(meResource);
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
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/me/\"}"));
    assertTrue(response.getContentAsString().contains("\"delete\":{\"href\":\"/v2/users/trillian\"}"));
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldEncryptPasswordBeforeChanging() throws Exception {
    String newPassword = "pwd123";
    String encryptedNewPassword = "encrypted123";
    String oldPassword = "secret";
    String content = String.format("{ \"oldPassword\": \"%s\" , \"newPassword\": \"%s\" }", oldPassword, newPassword);
    MockHttpRequest request = MockHttpRequest
      .put("/" + MeResource.ME_PATH_V2 + "password")
      .contentType(VndMediaType.PASSWORD_CHANGE)
      .content(content.getBytes());
    MockHttpResponse response = new MockHttpResponse();
    when(passwordService.encryptPassword(newPassword)).thenReturn(encryptedNewPassword);
    when(passwordService.encryptPassword(oldPassword)).thenReturn("secret");
    ArgumentCaptor<User> modifyUserCaptor = ArgumentCaptor.forClass(User.class);
    doNothing().when(userManager).modify(modifyUserCaptor.capture(), any());

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    verify(userManager).modify(any(), any());
    User updatedUser = modifyUserCaptor.getValue();
    assertEquals(encryptedNewPassword, updatedUser.getPassword());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldGet400OnChangePasswordOfUserWithNonDefaultType() throws Exception {
    originalUser.setType("not an xml type");
    String newPassword = "pwd123";
    String oldPassword = "secret";
    String content = String.format("{ \"oldPassword\": \"%s\" , \"newPassword\": \"%s\" }", oldPassword, newPassword);
    MockHttpRequest request = MockHttpRequest
      .put("/" + MeResource.ME_PATH_V2 + "password")
      .contentType(VndMediaType.PASSWORD_CHANGE)
      .content(content.getBytes());
    MockHttpResponse response = new MockHttpResponse();
    when(passwordService.encryptPassword(eq(newPassword))).thenReturn("encrypted123");
    when(passwordService.encryptPassword(eq(oldPassword))).thenReturn("secret");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldGet400OnChangePasswordIfOldPasswordDoesNotMatchOriginalPassword() throws Exception {
    String newPassword = "pwd123";
    String oldPassword = "notEncriptedSecret";
    String content = String.format("{ \"oldPassword\": \"%s\" , \"newPassword\": \"%s\" }", oldPassword, newPassword);
    MockHttpRequest request = MockHttpRequest
      .put("/" + MeResource.ME_PATH_V2 + "password")
      .contentType(VndMediaType.PASSWORD_CHANGE)
      .content(content.getBytes());
    MockHttpResponse response = new MockHttpResponse();
    when(passwordService.encryptPassword(newPassword)).thenReturn("encrypted123");
    when(passwordService.encryptPassword(eq(oldPassword))).thenReturn("differentThanSecret");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
  }


  private User createDummyUser(String name) {
    User user = new User();
    user.setName(name);
    user.setType("xml");
    user.setPassword("secret");
    user.setCreationDate(System.currentTimeMillis());
    when(userManager.get(name)).thenReturn(user);
    return user;
  }
}
