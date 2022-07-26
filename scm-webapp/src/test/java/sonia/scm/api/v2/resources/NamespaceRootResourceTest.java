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

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.web.RestDispatcher;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static com.google.inject.util.Providers.of;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NamespaceRootResourceTest {

  @Mock
  RepositoryManager repositoryManager;
  @Mock
  NamespaceManager namespaceManager;
  @Mock
  Subject subject;

  RestDispatcher dispatcher = new RestDispatcher();
  MockHttpResponse response = new MockHttpResponse();

  ResourceLinks links = ResourceLinksMock.createMock(URI.create("/"));

  @InjectMocks
  RepositoryPermissionToRepositoryPermissionDtoMapperImpl repositoryPermissionToRepositoryPermissionDtoMapper;

  @BeforeEach
  void mockSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @BeforeEach
  void setUpResources() {
    NamespaceToNamespaceDtoMapper namespaceMapper = new NamespaceToNamespaceDtoMapper(links, null);
    NamespaceCollectionToDtoMapper namespaceCollectionToDtoMapper = new NamespaceCollectionToDtoMapper(namespaceMapper, links);
    RepositoryPermissionCollectionToDtoMapper repositoryPermissionCollectionToDtoMapper = new RepositoryPermissionCollectionToDtoMapper(repositoryPermissionToRepositoryPermissionDtoMapper, links);
    RepositoryPermissionDtoToRepositoryPermissionMapperImpl dtoToModelMapper = new RepositoryPermissionDtoToRepositoryPermissionMapperImpl();

    NamespaceCollectionResource namespaceCollectionResource = new NamespaceCollectionResource(repositoryManager, namespaceCollectionToDtoMapper);
    NamespacePermissionResource namespacePermissionResource = new NamespacePermissionResource(dtoToModelMapper, repositoryPermissionToRepositoryPermissionDtoMapper, repositoryPermissionCollectionToDtoMapper, links, namespaceManager);
    NamespaceResource namespaceResource = new NamespaceResource(repositoryManager, namespaceMapper, of(namespacePermissionResource));
    dispatcher.addSingletonResource(new NamespaceRootResource(of(namespaceCollectionResource), of(namespaceResource)));
  }

  @BeforeEach
  void mockExistingNamespaces() {
    lenient().when(repositoryManager.getAllNamespaces()).thenReturn(asList("hitchhiker", "space"));
    Namespace hitchhikerNamespace = new Namespace("hitchhiker");
    hitchhikerNamespace.setPermissions(singleton(new RepositoryPermission("humans", "READ", true)));
    Namespace spaceNamespace = new Namespace("space");
    lenient().when(namespaceManager.getAll()).thenReturn(asList(hitchhikerNamespace, spaceNamespace));
    lenient().when(namespaceManager.get("hitchhiker")).thenReturn(Optional.of(hitchhikerNamespace));
    lenient().when(namespaceManager.get("space")).thenReturn(Optional.of(spaceNamespace));
  }

  @Nested
  class WithoutSpecialPermission {

    @BeforeEach
    void mockNoPermissions() {
      lenient().when(subject.isPermitted(anyString())).thenReturn(false);
      lenient().doThrow(AuthorizationException.class).when(subject).checkPermission("namespace:permissionRead");
      lenient().doThrow(AuthorizationException.class).when(subject).checkPermission("namespace:permissionWrite");
    }

    @Test
    void shouldReturnAllNamespaces() throws URISyntaxException, UnsupportedEncodingException {
      MockHttpRequest request = MockHttpRequest.get("/" + NamespaceRootResource.NAMESPACE_PATH_V2);

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getContentAsString())
        .contains("\"self\":{\"href\":\"/v2/namespaces/\"}")
        .contains("\"_embedded\"")
        .contains("\"namespace\":\"hitchhiker\"")
        .contains("\"namespace\":\"space\"");
    }

    @Test
    void shouldReturnSingleNamespace() throws URISyntaxException, UnsupportedEncodingException {
      MockHttpRequest request = MockHttpRequest.get("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "space");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getContentAsString())
        .contains("\"namespace\":\"space\"")
        .contains("\"self\":{\"href\":\"/v2/namespaces/space\"}")
        .doesNotContain("permissions");
    }

    @Test
    void shouldHandleUnknownNamespace() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "unknown");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void shouldNotReturnPermissions() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "space/permissions");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(403);
    }
  }

  @Nested
  class WithReadPermission {

    @BeforeEach
    void grantReadPermission() {
      lenient().when(subject.isPermitted("namespace:permissionRead")).thenReturn(true);
      lenient().when(subject.isPermitted("namespace:permissionWrite")).thenReturn(false);
      lenient().doThrow(AuthorizationException.class).when(subject).checkPermission("namespace:permissionWrite");
    }

    @Test
    void shouldContainPermissionLinkWhenPermitted() throws UnsupportedEncodingException, URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "space");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getContentAsString())
        .contains("\"permissions\":{\"href\":\"/v2/namespaces/space/permissions\"}");
    }

    @Test
    void shouldReturnPermissions() throws UnsupportedEncodingException, URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "hitchhiker/permissions");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getContentAsString())
        .contains("\"self\":{\"href\":\"/v2/namespaces/hitchhiker/permissions\"}")
        .contains("{\"name\":\"humans\",\"verbs\":[],\"role\":\"READ\",\"groupPermission\":true,\"")
        .doesNotContain("create");
    }

    @Test
    void shouldReturnSinglePermission() throws UnsupportedEncodingException, URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "hitchhiker/permissions/@humans");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getContentAsString())
        .contains("\"self\":{\"href\":\"/v2/namespaces/hitchhiker/permissions/@humans\"}")
        .contains("{\"name\":\"humans\",\"verbs\":[],\"role\":\"READ\",\"groupPermission\":true,\"")
        .doesNotContain("update")
        .doesNotContain("delete");
    }

    @Test
    void shouldHandleMissingNamespace() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "no_such_namespace/permissions");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(404);
    }

    @Nested
    class WithWritePermission {

      @BeforeEach
      void grantWritePermission() {
        lenient().when(subject.isPermitted("namespace:permissionWrite")).thenReturn(true);
        lenient().doNothing().when(subject).checkPermission("namespace:permissionWrite");
      }

      @Test
      void shouldContainCreateLink() throws UnsupportedEncodingException, URISyntaxException {
        MockHttpRequest request = MockHttpRequest.get("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "space/permissions");

        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString())
          .contains("\"create\":{\"href\":\"/v2/namespaces/space/permissions\"}");
      }

      @Test
      void shouldContainModificationLinks() throws UnsupportedEncodingException, URISyntaxException {
        MockHttpRequest request = MockHttpRequest.get("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "hitchhiker/permissions/@humans");

        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString())
          .contains("\"update\":{\"href\":\"/v2/namespaces/hitchhiker/permissions/@humans\"")
          .contains("\"delete\":{\"href\":\"/v2/namespaces/hitchhiker/permissions/@humans\"");
      }

      @Test
      void shouldCreateNewPermission() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "space/permissions")
          .content("{\"name\":\"dent\",\"verbs\":[],\"role\":\"WRITE\",\"groupPermission\":false}".getBytes())
          .header("Content-Type", "application/vnd.scmm-repositoryPermission+json;v=2");

        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(201);
        verify(namespaceManager)
          .modify(argThat(
            namespace -> {
              assertThat(namespace.getPermissions()).hasSize(1);
              RepositoryPermission permission = namespace.getPermissions().iterator().next();
              assertThat(permission.getName()).isEqualTo("dent");
              assertThat(permission.getRole()).isEqualTo("WRITE");
              assertThat(permission.getVerbs()).isEmpty();
              assertThat(permission.isGroupPermission()).isFalse();
              return true;
            })
          );
        assertThat(response.getOutputHeaders().get("Location"))
          .containsExactly(URI.create("/v2/namespaces/space/permissions/dent"));
      }

      @Test
      void shouldUpdatePermission() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.put("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "hitchhiker/permissions/@humans")
          .content("{\"name\":\"humans\",\"verbs\":[],\"role\":\"WRITE\",\"groupPermission\":true}".getBytes())
          .header("Content-Type", "application/vnd.scmm-repositoryPermission+json;v=2");

        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(204);
        verify(namespaceManager)
          .modify(argThat(
            namespace -> {
              assertThat(namespace.getPermissions()).hasSize(1);
              RepositoryPermission permission = namespace.getPermissions().iterator().next();
              assertThat(permission.getName()).isEqualTo("humans");
              assertThat(permission.getRole()).isEqualTo("WRITE");
              assertThat(permission.getVerbs()).isEmpty();
              assertThat(permission.isGroupPermission()).isTrue();
              return true;
            })
          );
      }

      @Test
      void shouldHandleNotExistingPermissionOnUpdate() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.put("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "hitchhiker/permissions/humans")
          .content("{\"name\":\"humans\",\"verbs\":[],\"role\":\"WRITE\",\"groupPermission\":true}".getBytes())
          .header("Content-Type", "application/vnd.scmm-repositoryPermission+json;v=2");

        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(404);
      }

      @Test
      void shouldHandleExistingPermissionOnCreate() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "hitchhiker/permissions")
          .content("{\"name\":\"humans\",\"verbs\":[],\"role\":\"WRITE\",\"groupPermission\":true}".getBytes())
          .header("Content-Type", "application/vnd.scmm-repositoryPermission+json;v=2");

        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(409);
        verify(namespaceManager, never()).modify(any());
      }

      @Test
      void shouldDeleteExistingPermission() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.delete("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "hitchhiker/permissions/@humans");

        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(204);
        verify(namespaceManager)
          .modify(argThat(
            namespace -> {
              assertThat(namespace.getPermissions()).isEmpty();
              return true;
            })
          );
      }

      @Test
      void shouldHandleRedundantDeleteIdempotent() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.delete("/" + NamespaceRootResource.NAMESPACE_PATH_V2 + "hitchhiker/permissions/humans");

        dispatcher.invoke(request, response);

        assertThat(response.getStatus()).isEqualTo(204);
        verify(namespaceManager, never()).modify(any());
      }
    }
  }
}
