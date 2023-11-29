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
import jakarta.servlet.http.HttpServletResponse;
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
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.GroupCollector;
import sonia.scm.security.ApiKey;
import sonia.scm.security.ApiKeyService;
import sonia.scm.user.EMail;
import sonia.scm.user.InvalidPasswordException;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.inject.util.Providers.of;
import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
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

  private final RestDispatcher dispatcher = new RestDispatcher();
  private final MockHttpResponse response = new MockHttpResponse();
  private final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("/"));

  @Mock
  private ScmPathInfo uriInfo;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;

  @Mock
  private GroupCollector groupCollector;

  @Mock
  private UserManager userManager;

  @Mock
  private ApiKeyService apiKeyService;

  @Mock
  private ScmConfiguration scmConfiguration;

  @Mock
  private EMail eMail;

  @InjectMocks
  private MeDtoFactory meDtoFactory;
  @InjectMocks
  private ApiKeyToApiKeyDtoMapperImpl apiKeyMapper;

  @Mock
  private PasswordService passwordService;
  private User originalUser;

  @Mock
  private NotificationResource notificationResource;

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
    ApiKeyCollectionToDtoMapper apiKeyCollectionMapper = new ApiKeyCollectionToDtoMapper(apiKeyMapper, resourceLinks);
    ApiKeyResource apiKeyResource = new ApiKeyResource(apiKeyService, apiKeyCollectionMapper, apiKeyMapper, resourceLinks);
    MeResource meResource = new MeResource(meDtoFactory, userManager, passwordService, of(apiKeyResource), of(notificationResource));
    when(uriInfo.getApiRestUri()).thenReturn(URI.create("/"));
    when(scmPathInfoStore.get()).thenReturn(uriInfo);
    dispatcher.addSingletonResource(meResource);
  }

  @Test
  public void shouldReturnCurrentlyAuthenticatedUser() throws URISyntaxException, UnsupportedEncodingException {
    applyUserToSubject(originalUser);
    when(scmConfiguration.isEnabledApiKeys()).thenReturn(true);

    MockHttpRequest request = MockHttpRequest.get("/" + MeResource.ME_PATH_V2);
    request.accept(VndMediaType.ME);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertThat(response.getContentAsString()).contains("\"name\":\"trillian\"");
    assertThat(response.getContentAsString()).contains("\"self\":{\"href\":\"/v2/me/\"}");
    assertThat(response.getContentAsString()).contains("\"delete\":{\"href\":\"/v2/users/trillian\"}");
    assertThat(response.getContentAsString()).contains("\"apiKeys\":{\"href\":\"/v2/users/trillian/api_keys\"}");
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

    doThrow(new InvalidPasswordException(ContextEntry.ContextBuilder.entity("passwortChange", "-")))
      .when(userManager).changePasswordForLoggedInUser(any(), any());

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
  }

  @Test
  public void shouldGetAllApiKeys() throws URISyntaxException, UnsupportedEncodingException {
    when(apiKeyService.getKeys("trillian"))
      .thenReturn(asList(
        new ApiKey("1", "key 1", "READ", now()),
        new ApiKey("2", "key 2", "WRITE", now())));

    MockHttpRequest request = MockHttpRequest.get("/" + MeResource.ME_PATH_V2 + "api_keys");
    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    assertThat(response.getContentAsString()).contains("\"displayName\":\"key 1\",\"permissionRole\":\"READ\"");
    assertThat(response.getContentAsString()).contains("\"displayName\":\"key 2\",\"permissionRole\":\"WRITE\"");
    assertThat(response.getContentAsString()).contains("\"self\":{\"href\":\"/v2/users/trillian/api_keys\"}");
    assertThat(response.getContentAsString()).contains("\"create\":{\"href\":\"/v2/users/trillian/api_keys\"}");
  }

  @Test
  public void shouldGetSingleApiKey() throws URISyntaxException, UnsupportedEncodingException {
    when(apiKeyService.getKeys("trillian"))
      .thenReturn(asList(
        new ApiKey("1", "key 1", "READ", now()),
        new ApiKey("2", "key 2", "WRITE", now())));

    MockHttpRequest request = MockHttpRequest.get("/" + MeResource.ME_PATH_V2 + "api_keys/1");
    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    assertThat(response.getContentAsString()).contains("\"displayName\":\"key 1\"");
    assertThat(response.getContentAsString()).contains("\"permissionRole\":\"READ\"");
    assertThat(response.getContentAsString()).contains("\"self\":{\"href\":\"/v2/users/trillian/api_keys/1\"}");
    assertThat(response.getContentAsString()).contains("\"delete\":{\"href\":\"/v2/users/trillian/api_keys/1\"}");
  }

  @Test
  public void shouldCreateNewApiKey() throws URISyntaxException, UnsupportedEncodingException {
    when(apiKeyService.createNewKey("trillian","guide", "READ"))
      .thenReturn(new ApiKeyService.CreationResult("abc", "1"));

    final MockHttpRequest request = MockHttpRequest
      .post("/" + MeResource.ME_PATH_V2 + "api_keys/")
      .contentType(VndMediaType.API_KEY)
      .content("{\"displayName\":\"guide\",\"permissionRole\":\"READ\"}".getBytes());

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getContentAsString()).isEqualTo("abc");
    assertThat(response.getOutputHeaders().get("Location")).containsExactly(URI.create("/v2/users/trillian/api_keys/1"));
  }

  @Test
  public void shouldIgnoreInvalidNewApiKey() throws URISyntaxException {
    when(apiKeyService.createNewKey("trillian","guide", "READ"))
      .thenReturn(new ApiKeyService.CreationResult("abc", "1"));

    final MockHttpRequest request = MockHttpRequest
      .post("/" + MeResource.ME_PATH_V2 + "api_keys/")
      .contentType(VndMediaType.API_KEY)
      .content("{\"displayName\":\"guide\",\"permissionRole\":\"\"}".getBytes());

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void shouldDeleteExistingApiKey() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.delete("/" + MeResource.ME_PATH_V2 + "api_keys/1");
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(204);
    verify(apiKeyService).remove("trillian","1");
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
