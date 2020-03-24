/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.ContextEntry;
import sonia.scm.group.GroupCollector;
import sonia.scm.user.InvalidPasswordException;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class MeResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private RestDispatcher dispatcher = new RestDispatcher();

  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("/"));

  @Mock
  private ScmPathInfo uriInfo;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;

  @Mock
  private GroupCollector groupCollector;

  @Mock
  private UserManager userManager;

  @InjectMocks
  private MeDtoFactory meDtoFactory;

  private ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

  @Mock
  private PasswordService passwordService;
  private User originalUser;

  @Before
  public void prepareEnvironment() {
    initMocks(this);
    originalUser = createDummyUser("trillian");
    when(userManager.create(userCaptor.capture())).thenAnswer(invocation -> invocation.getArguments()[0]);
    doNothing().when(userManager).modify(userCaptor.capture());
    doNothing().when(userManager).delete(userCaptor.capture());
    when(groupCollector.collect("trillian")).thenReturn(ImmutableSet.of("group1", "group2"));
    when(userManager.isTypeDefault(userCaptor.capture())).thenCallRealMethod();
    when(userManager.getDefaultType()).thenReturn("xml");
    MeResource meResource = new MeResource(meDtoFactory, userManager, passwordService);
    when(uriInfo.getApiRestUri()).thenReturn(URI.create("/"));
    when(scmPathInfoStore.get()).thenReturn(uriInfo);
    dispatcher.addSingletonResource(meResource);
  }

  @Test
  public void shouldReturnCurrentlyAuthenticatedUser() throws URISyntaxException, UnsupportedEncodingException {
    applyUserToSubject(originalUser);

    MockHttpRequest request = MockHttpRequest.get("/" + MeResource.ME_PATH_V2);
    request.accept(VndMediaType.ME);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"trillian\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/me/\"}"));
    assertTrue(response.getContentAsString().contains("\"delete\":{\"href\":\"/v2/users/trillian\"}"));
  }

  private void applyUserToSubject(User user) {
    // use spy here to keep applied permissions from ShiroRule
    Subject subject = spy(SecurityUtils.getSubject());
    PrincipalCollection collection = mock(PrincipalCollection.class);
    when(collection.getPrimaryPrincipal()).thenReturn(user.getName());
    when(subject.getPrincipals()).thenReturn(collection);
    when(collection.oneByType(User.class)).thenReturn(user);
    shiro.setSubject(subject);
  }

  @Test
  public void shouldEncryptPasswordBeforeChanging() throws Exception {
    String newPassword = "pwd123";
    String encryptedNewPassword = "encrypted123";
    String encryptedOldPassword = "encryptedOld";
    String oldPassword = "secret";
    String content = String.format("{ \"oldPassword\": \"%s\" , \"newPassword\": \"%s\" }", oldPassword, newPassword);
    MockHttpRequest request = MockHttpRequest
      .put("/" + MeResource.ME_PATH_V2 + "password")
      .contentType(VndMediaType.PASSWORD_CHANGE)
      .content(content.getBytes());
    MockHttpResponse response = new MockHttpResponse();

    when(passwordService.encryptPassword(newPassword)).thenReturn(encryptedNewPassword);
    when(passwordService.encryptPassword(oldPassword)).thenReturn(encryptedOldPassword);

    ArgumentCaptor<String> encryptedOldPasswordCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> encryptedNewPasswordCaptor = ArgumentCaptor.forClass(String.class);
    doNothing().when(userManager).changePasswordForLoggedInUser(encryptedOldPasswordCaptor.capture(), encryptedNewPasswordCaptor.capture());

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    assertEquals(encryptedNewPassword, encryptedNewPasswordCaptor.getValue());
    assertEquals(encryptedOldPassword, encryptedOldPasswordCaptor.getValue());
  }

  @Test
  public void shouldGet400OnMissingOldPassword() throws Exception {
    originalUser.setType("not an xml type");
    String newPassword = "pwd123";
    String content = String.format("{ \"newPassword\": \"%s\" }", newPassword);
    MockHttpRequest request = MockHttpRequest
      .put("/" + MeResource.ME_PATH_V2 + "password")
      .contentType(VndMediaType.PASSWORD_CHANGE)
      .content(content.getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
  }

  @Test
  public void shouldGet400OnMissingEmptyPassword() throws Exception {
    String newPassword = "pwd123";
    String oldPassword = "";
    String content = String.format("{ \"oldPassword\": \"%s\" , \"newPassword\": \"%s\" }", oldPassword, newPassword);
    MockHttpRequest request = MockHttpRequest
      .put("/" + MeResource.ME_PATH_V2 + "password")
      .contentType(VndMediaType.PASSWORD_CHANGE)
      .content(content.getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
  }

  @Test
  public void shouldMapExceptionFromManager() throws Exception {
    String newPassword = "pwd123";
    String oldPassword = "secret";
    String content = String.format("{ \"oldPassword\": \"%s\" , \"newPassword\": \"%s\" }", oldPassword, newPassword);
    MockHttpRequest request = MockHttpRequest
      .put("/" + MeResource.ME_PATH_V2 + "password")
      .contentType(VndMediaType.PASSWORD_CHANGE)
      .content(content.getBytes());
    MockHttpResponse response = new MockHttpResponse();

    doThrow(new InvalidPasswordException(ContextEntry.ContextBuilder.entity("passwortChange", "-")))
      .when(userManager).changePasswordForLoggedInUser(any(), any());

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
