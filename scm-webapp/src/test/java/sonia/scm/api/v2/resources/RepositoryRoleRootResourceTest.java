/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.inject.util.Providers;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.PageResult;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Collections;

import static java.net.URI.create;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
@RunWith(MockitoJUnitRunner.Silent.class)
public class RepositoryRoleRootResourceTest {

  public static final String CUSTOM_ROLE = "customRole";
  public static final String SYSTEM_ROLE = "systemRole";
  public static final RepositoryRole CUSTOM_REPOSITORY_ROLE = new RepositoryRole(CUSTOM_ROLE, Collections.singleton("verb"), "xml");
  public static final RepositoryRole SYSTEM_REPOSITORY_ROLE = new RepositoryRole(SYSTEM_ROLE, Collections.singleton("admin"), "system");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(create("/"));

  @Rule
  public ShiroRule shiroRule = new ShiroRule();

  @Mock
  private RepositoryRoleManager repositoryRoleManager;

  @InjectMocks
  private RepositoryRoleToRepositoryRoleDtoMapperImpl roleToDtoMapper;

  @InjectMocks
  private RepositoryRoleDtoToRepositoryRoleMapperImpl dtoToRoleMapper;

  private RepositoryRoleCollectionToDtoMapper collectionToDtoMapper;

  private RestDispatcher dispatcher = new RestDispatcher();

  @Captor
  private ArgumentCaptor<RepositoryRole> modifyCaptor;
  @Captor
  private ArgumentCaptor<RepositoryRole> createCaptor;
  @Captor
  private ArgumentCaptor<RepositoryRole> deleteCaptor;

  @Before
  public void init() {
    collectionToDtoMapper = new RepositoryRoleCollectionToDtoMapper(roleToDtoMapper, resourceLinks);

    RepositoryRoleCollectionResource collectionResource = new RepositoryRoleCollectionResource(repositoryRoleManager, dtoToRoleMapper, collectionToDtoMapper, resourceLinks);
    RepositoryRoleResource roleResource = new RepositoryRoleResource(dtoToRoleMapper, roleToDtoMapper, repositoryRoleManager);
    RepositoryRoleRootResource rootResource = new RepositoryRoleRootResource(Providers.of(collectionResource), Providers.of(roleResource));

    doNothing().when(repositoryRoleManager).modify(modifyCaptor.capture());
    when(repositoryRoleManager.create(createCaptor.capture())).thenAnswer(invocation -> invocation.getArguments()[0]);
    doNothing().when(repositoryRoleManager).delete(deleteCaptor.capture());

    dispatcher.addSingletonResource(rootResource);

    when(repositoryRoleManager.get(CUSTOM_ROLE)).thenReturn(CUSTOM_REPOSITORY_ROLE);
    when(repositoryRoleManager.get(SYSTEM_ROLE)).thenReturn(SYSTEM_REPOSITORY_ROLE);
    when(repositoryRoleManager.getPage(any(), any(), anyInt(), anyInt())).thenReturn(new PageResult<>(asList(CUSTOM_REPOSITORY_ROLE, SYSTEM_REPOSITORY_ROLE), 2));
  }

  @Test
  public void shouldGetNotFoundForNotExistingRole() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + "noSuchRole");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  public void shouldGetCustomRole() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + CUSTOM_ROLE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString())
      .contains(
        "\"name\":\"" + CUSTOM_ROLE + "\"",
        "\"verbs\":[\"verb\"]",
        "\"self\":{\"href\":\"/v2/repositoryRoles/" + CUSTOM_ROLE + "\"}",
        "\"update\":{\"href\":\"/v2/repositoryRoles/" + CUSTOM_ROLE + "\"}",
        "\"delete\":{\"href\":\"/v2/repositoryRoles/" + CUSTOM_ROLE + "\"}"
      );
  }

  @Test
  public void shouldGetSystemRole() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + SYSTEM_ROLE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString())
      .contains(
        "\"name\":\"" + SYSTEM_ROLE + "\"",
        "\"verbs\":[\"admin\"]",
        "\"self\":{\"href\":\"/v2/repositoryRoles/" + SYSTEM_ROLE + "\"}"
      )
      .doesNotContain(
        "\"delete\":{\"href\":\"/v2/repositoryRoles/" + CUSTOM_ROLE + "\"}",
        "\"update\":{\"href\":\"/v2/repositoryRoles/" + CUSTOM_ROLE + "\"}"
      );
  }

  @Test
  @SubjectAware(username = "dent")
  public void shouldNotGetDeleteLinkWithoutPermission() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + CUSTOM_ROLE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString())
      .doesNotContain("delete");
  }

  @Test
  public void shouldUpdateRole() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + CUSTOM_ROLE)
      .contentType(VndMediaType.REPOSITORY_ROLE)
      .content(content("{'name': '" + CUSTOM_ROLE + "', 'verbs': ['write', 'push']}"));
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    verify(repositoryRoleManager).modify(any());
    assertThat(modifyCaptor.getValue().getName()).isEqualTo(CUSTOM_ROLE);
    assertThat(modifyCaptor.getValue().getVerbs()).containsExactly("write", "push");
  }

  @Test
  public void shouldNotChangeRoleName() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + CUSTOM_ROLE)
      .contentType(VndMediaType.REPOSITORY_ROLE)
      .content(content("{'name': 'changedName', 'verbs': ['write', 'push']}"));
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    verify(repositoryRoleManager, never()).modify(any());
  }

  @Test
  public void shouldFailForUpdateOfNotExistingRole() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + "noSuchRole")
      .contentType(VndMediaType.REPOSITORY_ROLE)
      .content(content("{'name': 'noSuchRole', 'verbs': ['write', 'push']}"));
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    verify(repositoryRoleManager, never()).modify(any());
  }

  @Test
  public void shouldCreateRole() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2)
      .contentType(VndMediaType.REPOSITORY_ROLE)
      .content(content("{'name': 'newRole', 'verbs': ['write', 'push']}"));
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_CREATED);
    verify(repositoryRoleManager).create(any());
    assertThat(createCaptor.getValue().getName()).isEqualTo("newRole");
    assertThat(createCaptor.getValue().getVerbs()).containsExactly("write", "push");
    Object location = response.getOutputHeaders().getFirst("Location");
    assertThat(location).isEqualTo(create("/v2/repositoryRoles/newRole"));
  }

  @Test
  public void shouldDeleteRole() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .delete("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2 + CUSTOM_ROLE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    verify(repositoryRoleManager).delete(any());
    assertThat(deleteCaptor.getValue().getName()).isEqualTo(CUSTOM_ROLE);
  }

  @Test
  public void shouldGetAllRoles() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString())
      .contains(
        "\"name\":\"" + CUSTOM_ROLE + "\"",
        "\"name\":\"" + SYSTEM_ROLE + "\"",
        "\"verbs\":[\"verb\"]",
        "\"verbs\":[\"admin\"]",
        "\"self\":{\"href\":\"/v2/repositoryRoles",
        "\"delete\":{\"href\":\"/v2/repositoryRoles/" + CUSTOM_ROLE + "\"}",
        "\"create\":{\"href\":\"/v2/repositoryRoles/\"}"
      )
    .doesNotContain(
      "\"delete\":{\"href\":\"/v2/repositoryRoles/" + SYSTEM_ROLE + "\"}"
    );
  }

  @Test
  public void shouldFailForEmptyName() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2)
      .contentType(VndMediaType.REPOSITORY_ROLE)
      .content(content("{'name': '', 'verbs': ['write', 'push']}"));
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    verify(repositoryRoleManager, never()).create(any());
  }

  @Test
  public void shouldFailForMissingVerbs() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2)
      .contentType(VndMediaType.REPOSITORY_ROLE)
      .content(content("{'name': 'ok', 'verbs': []}"));
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    verify(repositoryRoleManager, never()).create(any());
  }

  @Test
  public void shouldFailForEmptyVerb() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2)
      .contentType(VndMediaType.REPOSITORY_ROLE)
      .content(content("{'name': 'ok', 'verbs': ['', 'push']}"));
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
    verify(repositoryRoleManager, never()).create(any());
  }

  @Test
  @SubjectAware(username = "dent")
  public void shouldNotGetCreateLinkWithoutPermission() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRoleRootResource.REPOSITORY_ROLES_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString())
      .doesNotContain(
        "create"
      );
  }

  private byte[] content(String data) {
    return data.replaceAll("'", "\"").getBytes();
  }
}
