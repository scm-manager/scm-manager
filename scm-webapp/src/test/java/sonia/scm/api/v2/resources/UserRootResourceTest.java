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
import com.google.common.io.Resources;
import com.google.inject.util.Providers;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.credential.PasswordService;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.ContextEntry;
import sonia.scm.NotFoundException;
import sonia.scm.PageResult;
import sonia.scm.admin.ScmConfigurationStore;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.GroupManager;
import sonia.scm.security.ApiKeyService;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.ChangePasswordNotAllowedException;
import sonia.scm.user.PermissionOverview;
import sonia.scm.user.PermissionOverviewCollector;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.function.Predicate;

import static de.otto.edison.hal.Links.emptyLinks;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
@ExtendWith(MockitoExtension.class)
public class UserRootResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private final RestDispatcher dispatcher = new RestDispatcher();

  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("/"));

  @Mock
  private PasswordService passwordService;
  @Mock
  private UserManager userManager;
  @Mock
  private ApiKeyService apiKeyService;
  @Mock
  private PermissionAssigner permissionAssigner;
  @Mock
  private PermissionOverviewCollector permissionOverviewCollector;
  @Mock
  private RepositoryToRepositoryDtoMapper repositoryToRepositoryDtoMapper;
  @Mock
  private NamespaceToNamespaceDtoMapper namespaceToNamespaceDtoMapper;
  @Mock
  private GroupManager groupManager;
  @Mock
  private GroupToGroupDtoMapper groupToGroupDtoMapper;
  @Mock
  private ScmConfigurationStore scmConfigurationStore;
  @Mock
  private ScmConfiguration scmConfiguration;
  @InjectMocks
  private UserDtoToUserMapperImpl dtoToUserMapper;
  @InjectMocks
  private UserToUserDtoMapperImpl userToDtoMapper;
  @InjectMocks
  private PermissionCollectionToDtoMapper permissionCollectionToDtoMapper;
  @InjectMocks
  private ApiKeyToApiKeyDtoMapperImpl apiKeyMapper;
  @InjectMocks
  private PermissionOverviewToPermissionOverviewDtoMapperImpl permissionOverviewMapper;

  @Captor
  private ArgumentCaptor<User> userCaptor;
  @Captor
  private ArgumentCaptor<Predicate<User>> filterCaptor;

  private User originalUser;
  private MockHttpResponse response = new MockHttpResponse();

  @Before
  public void prepareEnvironment() {
    initMocks(this);
    when(scmConfigurationStore.get()).thenReturn(scmConfiguration);
    originalUser = createDummyUser("Neo");
    when(userManager.create(userCaptor.capture())).thenAnswer(invocation -> invocation.getArguments()[0]);
    when(userManager.isTypeDefault(userCaptor.capture())).thenCallRealMethod();
    doNothing().when(userManager).modify(userCaptor.capture());
    doNothing().when(userManager).delete(userCaptor.capture());
    when(userManager.getDefaultType()).thenReturn("xml");

    UserCollectionToDtoMapper userCollectionToDtoMapper = new UserCollectionToDtoMapper(userToDtoMapper, resourceLinks);
    UserCollectionResource userCollectionResource = new UserCollectionResource(userManager, dtoToUserMapper,
      userCollectionToDtoMapper, resourceLinks, passwordService);
    UserPermissionResource userPermissionResource = new UserPermissionResource(permissionAssigner, permissionCollectionToDtoMapper);
    UserResource userResource = new UserResource(dtoToUserMapper, userToDtoMapper, permissionOverviewMapper, userManager, passwordService, userPermissionResource, permissionOverviewCollector);
    ApiKeyCollectionToDtoMapper apiKeyCollectionToDtoMapper = new ApiKeyCollectionToDtoMapper(apiKeyMapper, resourceLinks);
    UserApiKeyResource userApiKeyResource = new UserApiKeyResource(apiKeyService, apiKeyCollectionToDtoMapper, apiKeyMapper, resourceLinks);
    UserRootResource userRootResource = new UserRootResource(Providers.of(userCollectionResource),
      Providers.of(userResource), Providers.of(userApiKeyResource));

    dispatcher.addSingletonResource(userRootResource);
  }

  @Test
  public void shouldCreateFullResponseForAdmin() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2 + "Neo");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"Neo\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/users/Neo\"}"));
    assertTrue(response.getContentAsString().contains("\"delete\":{\"href\":\"/v2/users/Neo\"}"));
  }

  @Test
  public void shouldGet400OnCreatingNewUserWithNotAllowedCharacters() throws URISyntaxException {
    // the @ character at the begin of the name is not allowed
    String userJson = "{ \"name\": \"@user\",\"active\": true,\"admin\": false,\"displayName\": \"someone\",\"mail\": \"x@example.com\",\"type\": \"db\" }";
    MockHttpRequest request = MockHttpRequest
      .post("/" + UserRootResource.USERS_PATH_V2)
      .contentType(VndMediaType.USER)
      .content(userJson.getBytes());

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());

    // the whitespace at the begin opf the name is not allowed
    userJson = "{ \"name\": \" user\",\"active\": true,\"admin\": false,\"displayName\": \"someone\",\"mail\": \"x@example.com\",\"type\": \"db\" }";
    request = MockHttpRequest
      .post("/" + UserRootResource.USERS_PATH_V2)
      .contentType(VndMediaType.USER)
      .content(userJson.getBytes());

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  @SubjectAware(username = "unpriv")
  public void shouldCreateLimitedResponseForSimpleUser() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2 + "Neo");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"Neo\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/users/Neo\"}"));
    assertFalse(response.getContentAsString().contains("\"delete\":{\"href\":\"/v2/users/Neo\"}"));
  }

  @Test
  public void shouldEncryptPasswordBeforeChanging() throws Exception {
    String newPassword = "pwd123";
    String content = String.format("{\"newPassword\": \"%s\"}", newPassword);
    MockHttpRequest request = MockHttpRequest
      .put("/" + UserRootResource.USERS_PATH_V2 + "Neo/password")
      .contentType(VndMediaType.PASSWORD_OVERWRITE)
      .content(content.getBytes());
    when(passwordService.encryptPassword(newPassword)).thenReturn("encrypted123");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    verify(userManager).overwritePassword("Neo", "encrypted123");
  }

  @Test
  public void shouldGet400OnOverwritePasswordWhenManagerThrowsNotAllowed() throws Exception {
    originalUser.setType("not an xml type");
    String newPassword = "pwd123";
    String content = String.format("{\"newPassword\": \"%s\"}", newPassword);
    MockHttpRequest request = MockHttpRequest
      .put("/" + UserRootResource.USERS_PATH_V2 + "Neo/password")
      .contentType(VndMediaType.PASSWORD_OVERWRITE)
      .content(content.getBytes());

    doThrow(new ChangePasswordNotAllowedException(ContextEntry.ContextBuilder.entity("passwordChange", "-"), "xml")).when(userManager).overwritePassword(any(), any());

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
  }

  @Test
  public void shouldGet404OnOverwritePasswordWhenNotFound() throws Exception {
    originalUser.setType("not an xml type");
    String newPassword = "pwd123";
    String content = String.format("{\"newPassword\": \"%s\"}", newPassword);
    MockHttpRequest request = MockHttpRequest
      .put("/" + UserRootResource.USERS_PATH_V2 + "Neo/password")
      .contentType(VndMediaType.PASSWORD_OVERWRITE)
      .content(content.getBytes());

    doThrow(new NotFoundException("Test", "x")).when(userManager).overwritePassword(any(), any());

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldEncryptPasswordOnOverwritePassword() throws Exception {
    originalUser.setType("not an xml type");
    String newPassword = "pwd123";
    String content = String.format("{\"newPassword\": \"%s\"}", newPassword);
    MockHttpRequest request = MockHttpRequest
      .put("/" + UserRootResource.USERS_PATH_V2 + "Neo/password")
      .contentType(VndMediaType.PASSWORD_OVERWRITE)
      .content(content.getBytes());
    when(passwordService.encryptPassword(newPassword)).thenReturn("encrypted123");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    verify(userManager).overwritePassword("Neo", "encrypted123");
  }

  @Test
  public void shouldEncryptPasswordBeforeCreatingUser() throws Exception {
    URL url = Resources.getResource("sonia/scm/api/v2/user-test-create.json");
    byte[] userJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + UserRootResource.USERS_PATH_V2)
      .contentType(VndMediaType.USER)
      .content(userJson);
    when(passwordService.encryptPassword("pwd123")).thenReturn("encrypted123");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
    verify(userManager).create(any(User.class));
    User createdUser = userCaptor.getValue();
    assertEquals("encrypted123", createdUser.getPassword());
  }

  @Test
  public void shouldIgnoreGivenPasswordOnUpdatingUser() throws Exception {
    URL url = Resources.getResource("sonia/scm/api/v2/user-test-update.json");
    byte[] userJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .put("/" + UserRootResource.USERS_PATH_V2 + "Neo")
      .contentType(VndMediaType.USER)
      .content(userJson);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    verify(userManager).modify(any(User.class));
    User updatedUser = userCaptor.getValue();
    assertEquals(originalUser.getPassword(), updatedUser.getPassword());
  }

  @Test
  public void shouldFailForMissingContent() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .post("/" + UserRootResource.USERS_PATH_V2)
      .contentType(VndMediaType.USER)
      .content(new byte[]{});
    when(passwordService.encryptPassword("pwd123")).thenReturn("encrypted123");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
  }

  @Test
  public void shouldGetNotFoundForNotExistentUser() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2 + "nosuchuser");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldDeleteUser() throws Exception {
    MockHttpRequest request = MockHttpRequest.delete("/" + UserRootResource.USERS_PATH_V2 + "Neo");

    dispatcher.invoke(request, response);

    verify(userManager).delete(any(User.class));
    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
  }

  @Test
  public void shouldFailUpdateForDifferentIds() throws Exception {
    URL url = Resources.getResource("sonia/scm/api/v2/user-test-update.json");
    byte[] userJson = Resources.toByteArray(url);
    createDummyUser("Other");

    MockHttpRequest request = MockHttpRequest
      .put("/" + UserRootResource.USERS_PATH_V2 + "Other")
      .contentType(VndMediaType.USER)
      .content(userJson);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    verify(userManager, never()).modify(any(User.class));
  }

  @Test
  public void shouldFailUpdateForUnknownEntity() throws Exception {
    URL url = Resources.getResource("sonia/scm/api/v2/user-test-update.json");
    byte[] userJson = Resources.toByteArray(url);
    when(userManager.get("Neo")).thenReturn(null);

    MockHttpRequest request = MockHttpRequest
      .put("/" + UserRootResource.USERS_PATH_V2 + "Neo")
      .contentType(VndMediaType.USER)
      .content(userJson);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    verify(userManager, never()).modify(any(User.class));
  }

  @Test
  public void shouldCreatePageForOnePageOnly() throws URISyntaxException, UnsupportedEncodingException {
    PageResult<User> singletonPageResult = createSingletonPageResult(1);
    when(userManager.getPage(any(), any(), eq(0), eq(10))).thenReturn(singletonPageResult);
    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"Neo\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/users/?page=0"));
    assertTrue(response.getContentAsString().contains("\"create\":{\"href\":\"/v2/users/\"}"));
    assertFalse(response.getContentAsString().contains("\"next\"")); // check for bug of edison-hal v2.0.0
  }

  @Test
  public void shouldCreatePageForMultiplePages() throws URISyntaxException, UnsupportedEncodingException {
    PageResult<User> singletonPageResult = createSingletonPageResult(3);
    when(userManager.getPage(any(), any(), eq(1), eq(1))).thenReturn(singletonPageResult);
    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2 + "?page=1&pageSize=1");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"Neo\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/users/?page=1"));
    assertTrue(response.getContentAsString().contains("\"first\":{\"href\":\"/v2/users/?page=0"));
    assertTrue(response.getContentAsString().contains("\"prev\":{\"href\":\"/v2/users/?page=0"));
    assertTrue(response.getContentAsString().contains("\"next\":{\"href\":\"/v2/users/?page=2"));
    assertTrue(response.getContentAsString().contains("\"last\":{\"href\":\"/v2/users/?page=2"));
  }

  @Test
  public void shouldCreateFilterForSearch() throws URISyntaxException {
    PageResult<User> singletonPageResult = createSingletonPageResult(1);
    when(userManager.getPage(filterCaptor.capture(), any(), eq(0), eq(10))).thenReturn(singletonPageResult);
    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2 + "?q=One");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    User user = new User("Someone I know");
    assertTrue(filterCaptor.getValue().test(user));
    user.setName("nobody");
    user.setDisplayName("Someone I know");
    assertTrue(filterCaptor.getValue().test(user));
    user.setDisplayName("nobody");
    user.setMail("me@someone.com");
    assertTrue(filterCaptor.getValue().test(user));
    user.setMail("me@nowhere.com");
    assertFalse(filterCaptor.getValue().test(user));
  }

  @Test
  public void shouldGetPermissionLink() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2 + "Neo");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    assertTrue(response.getContentAsString().contains("\"permissions\":{"));
  }

  @Test
  public void shouldGetPermissions() throws URISyntaxException, UnsupportedEncodingException {
    when(permissionAssigner.readPermissionsForUser("Neo")).thenReturn(singletonList(new PermissionDescriptor("something:*")));
    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2 + "Neo/permissions");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    assertTrue(response.getContentAsString().contains("\"permissions\":[\"something:*\"]"));
  }

  @Test
  public void shouldSetPermissions() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .put("/" + UserRootResource.USERS_PATH_V2 + "Neo/permissions")
      .contentType(VndMediaType.PERMISSION_COLLECTION)
      .content("{\"permissions\":[\"other:*\"]}".getBytes());
    ArgumentCaptor<Collection<PermissionDescriptor>> captor = ArgumentCaptor.forClass(Collection.class);
    doNothing().when(permissionAssigner).setPermissionsForUser(eq("Neo"), captor.capture());

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());

    assertEquals("other:*", captor.getValue().iterator().next().getValue());
  }

  @Test
  public void shouldGetPermissionsOverviewWithNamespaces() throws URISyntaxException, UnsupportedEncodingException {
    when(permissionOverviewCollector.create("Neo")).thenReturn(new PermissionOverview(emptyList(), singletonList("hog"), emptyList()));
    when(namespaceToNamespaceDtoMapper.map("hog")).thenReturn(new NamespaceDto("hog", emptyLinks()));
    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2 + "Neo/permissionOverview");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("hog");
    assertThat(response.getContentAsString()).contains("\"self\":{\"href\":\"/v2/users/Neo/permissionOverview\"}");
  }

  @Test
  public void shouldConvertUserToInternalAndSetNewPassword() throws URISyntaxException {
    when(passwordService.encryptPassword(anyString())).thenReturn("abc");
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    MockHttpRequest request = MockHttpRequest
      .put("/" + UserRootResource.USERS_PATH_V2 + "Neo/convert-to-internal")
      .contentType(VndMediaType.USER)
      .content("{\"newPassword\":\"trillian\"}".getBytes());

    dispatcher.invoke(request, response);

    verify(passwordService).encryptPassword("trillian");
    verify(userManager).overwritePassword("Neo", "abc");
    verify(userManager).modify(userCaptor.capture());

    User user = userCaptor.getValue();
    assertThat(user.isExternal()).isFalse();
  }

  @Test
  public void shouldConvertUserToExternalAndRemoveLocalPassword() throws URISyntaxException {
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    MockHttpRequest request = MockHttpRequest
      .put("/" + UserRootResource.USERS_PATH_V2 + "Neo/convert-to-external")
      .contentType(VndMediaType.USER);

    dispatcher.invoke(request, response);

    verify(userManager).overwritePassword("Neo", null);
    verify(userManager).modify(userCaptor.capture());

    User user = userCaptor.getValue();
    assertThat(user.isExternal()).isTrue();
  }

  private PageResult<User> createSingletonPageResult(int overallCount) {
    return new PageResult<>(singletonList(createDummyUser("Neo")), overallCount);
  }

  private User createDummyUser(String name) {
    User user = new User();
    user.setName(name);
    user.setType("xml");
    user.setPassword("redpill");
    user.setCreationDate(System.currentTimeMillis());
    when(userManager.get(name)).thenReturn(user);
    return user;
  }
}
