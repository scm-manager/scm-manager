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
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.web.GitVndMediaType;
import sonia.scm.web.JsonMockHttpRequest;
import sonia.scm.web.RestDispatcher;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.inject.util.Providers.of;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SubjectAware(
  configuration = "classpath:sonia/scm/configuration/shiro.ini",
  password = "secret"
)
@RunWith(MockitoJUnitRunner.class)
public class GitConfigResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private final RestDispatcher dispatcher = new RestDispatcher();

  private final URI baseUri = URI.create("/");

  @InjectMocks
  private GitConfigDtoToGitConfigMapperImpl dtoToConfigMapper;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @Mock
  private RepositoryManager repositoryManager;

  @InjectMocks
  private GitConfigToGitConfigDtoMapperImpl configToDtoMapper;
  @InjectMocks
  private GitRepositoryConfigMapperImpl repositoryConfigMapper;

  @Mock
  private GitRepositoryHandler repositoryHandler;

  @Mock(answer = Answers.CALLS_REAL_METHODS)
  private ConfigurationStoreFactory configurationStoreFactory;
  @Spy
  private ConfigurationStore<Object> configurationStore;
  @Captor
  private ArgumentCaptor<Object> configurationStoreCaptor;

  @Before
  public void prepareEnvironment() {
    GitConfig gitConfig = createConfiguration();
    when(repositoryHandler.getConfig()).thenReturn(gitConfig);
    GitRepositoryConfigResource gitRepositoryConfigResource = new GitRepositoryConfigResource(repositoryConfigMapper, repositoryManager, new GitRepositoryConfigStoreProvider(configurationStoreFactory), repositoryHandler);
    GitConfigResource gitConfigResource = new GitConfigResource(dtoToConfigMapper, configToDtoMapper, repositoryHandler, of(gitRepositoryConfigResource));
    dispatcher.addSingletonResource(gitConfigResource);
    when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
  }

  @Before
  public void initConfigStore() {
    when(configurationStoreFactory.getStore(any())).thenReturn(configurationStore);
    doNothing().when(configurationStore).set(configurationStoreCaptor.capture());
  }

  @Test
  @SubjectAware(username = "readWrite")
  public void shouldGetGitConfig() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpResponse response = get();

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    String responseString = response.getContentAsString();

    assertThat(responseString)
      .contains("\"disabled\":false")
      .contains("\"gcExpression\":\"valid Git GC Cron Expression\"")
      .contains("\"self\":{\"href\":\"/v2/config/git")
      .contains("\"update\":{\"href\":\"/v2/config/git")
      .contains("\"lfsWriteAuthorizationExpirationInMinutes\":5");
  }

  @Test
  @SubjectAware(username = "readWrite")
  public void shouldGetGitConfigEvenWhenItsEmpty() throws URISyntaxException, UnsupportedEncodingException {
    when(repositoryHandler.getConfig()).thenReturn(null);

    MockHttpResponse response = get();
    String responseString = response.getContentAsString();

    assertTrue(responseString.contains("\"disabled\":false"));
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetGitConfigWithoutUpdateLink() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpResponse response = get();

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    assertFalse(response.getContentAsString().contains("\"update\":{\"href\":\"/v2/config/git"));
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldNotGetConfigWhenNotAuthorized() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpResponse response = get();

    assertEquals("Subject does not have permission [configuration:read:git]", response.getContentAsString());
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldUpdateConfig() throws URISyntaxException {
    MockHttpResponse response = put();
    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldNotUpdateConfigWhenNotAuthorized() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpResponse response = put();

    assertEquals("Subject does not have permission [configuration:write:git]", response.getContentAsString());
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  @SubjectAware(username = "readWrite")
  public void shouldReadDefaultRepositoryConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(repositoryManager.get(new NamespaceAndName("space", "X"))).thenReturn(new Repository("id", "git", "space", "X"));

    MockHttpRequest request = MockHttpRequest.get("/" + GitConfigResource.GIT_CONFIG_PATH_V2 + "/space/X");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertThat(response.getContentAsString())
      .contains("\"defaultBranch\":null")
      .contains("self")
      .contains("update");
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldNotHaveUpdateLinkForReadOnlyUser() throws URISyntaxException, UnsupportedEncodingException {
    when(repositoryManager.get(new NamespaceAndName("space", "X"))).thenReturn(new Repository("id", "git", "space", "X"));

    MockHttpRequest request = MockHttpRequest.get("/" + GitConfigResource.GIT_CONFIG_PATH_V2 + "/space/X");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertThat(response.getContentAsString())
      .contains("\"defaultBranch\":null")
      .contains("self")
      .doesNotContain("update");
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldReadStoredRepositoryConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(repositoryManager.get(new NamespaceAndName("space", "X"))).thenReturn(new Repository("id", "git", "space", "X"));
    GitRepositoryConfig gitRepositoryConfig = new GitRepositoryConfig();
    gitRepositoryConfig.setDefaultBranch("test");
    when(configurationStore.get()).thenReturn(gitRepositoryConfig);

    MockHttpRequest request = MockHttpRequest.get("/" + GitConfigResource.GIT_CONFIG_PATH_V2 + "/space/X");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertThat(response.getContentAsString())
      .contains("\"defaultBranch\":\"test\"");
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldStoreChangedRepositoryConfig() throws URISyntaxException {
    when(repositoryManager.get(new NamespaceAndName("space", "X"))).thenReturn(new Repository("id", "git", "space", "X"));

    MockHttpRequest request = MockHttpRequest
      .put("/" + GitConfigResource.GIT_CONFIG_PATH_V2 + "/space/X")
      .contentType(GitVndMediaType.GIT_REPOSITORY_CONFIG)
      .content("{\"defaultBranch\": \"new\"}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    assertThat(configurationStoreCaptor.getValue())
      .isInstanceOfSatisfying(GitRepositoryConfig.class, x -> { })
      .extracting("defaultBranch")
      .isEqualTo("new");
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetDefaultBranchFromRepoConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(repositoryManager.get(new NamespaceAndName("space", "X"))).thenReturn(new Repository("id", "git", "space", "X"));
    when(configurationStore.get()).thenReturn(new GitRepositoryConfig("default"));
    MockHttpRequest request = MockHttpRequest
      .get("/" + GitConfigResource.GIT_CONFIG_PATH_V2 + "/space/X/default-branch")
      .contentType(GitVndMediaType.GIT_REPOSITORY_CONFIG);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertEquals("{\"defaultBranch\":\"default\"}", response.getContentAsString());
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetDefaultBranchFromGlobalConfig() throws URISyntaxException, UnsupportedEncodingException {
    when(repositoryManager.get(new NamespaceAndName("space", "X"))).thenReturn(new Repository("id", "git", "space", "X"));
    when(configurationStore.get()).thenReturn(new GitRepositoryConfig());
    GitConfig globalGitConfig = createConfiguration();
    globalGitConfig.setDefaultBranch("global-default");
    when(repositoryHandler.getConfig()).thenReturn(globalGitConfig);
    MockHttpRequest request = MockHttpRequest
      .get("/" + GitConfigResource.GIT_CONFIG_PATH_V2 + "/space/X/default-branch")
      .contentType(GitVndMediaType.GIT_REPOSITORY_CONFIG);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertEquals("{\"defaultBranch\":\"global-default\"}", response.getContentAsString());
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetFallbackDefaultBranchIfBothConfigsEmpty() throws URISyntaxException, UnsupportedEncodingException {
    when(repositoryManager.get(new NamespaceAndName("space", "X"))).thenReturn(new Repository("id", "git", "space", "X"));
    when(configurationStore.get()).thenReturn(new GitRepositoryConfig());
    when(repositoryHandler.getConfig()).thenReturn(createConfiguration());
    MockHttpRequest request = MockHttpRequest
      .get("/" + GitConfigResource.GIT_CONFIG_PATH_V2 + "/space/X/default-branch")
      .contentType(GitVndMediaType.GIT_REPOSITORY_CONFIG);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertEquals("{\"defaultBranch\":\"main\"}", response.getContentAsString());
  }

  @Test
  public void shouldThrowAuthorizationExceptionIfNotPermittedToGetDefaultBranch() throws URISyntaxException {
    when(repositoryManager.get(new NamespaceAndName("space", "X"))).thenReturn(new Repository("id", "git", "space", "X"));
    MockHttpRequest request = MockHttpRequest
      .get("/" + GitConfigResource.GIT_CONFIG_PATH_V2 + "/space/X/default-branch")
      .contentType(GitVndMediaType.GIT_REPOSITORY_CONFIG);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  private MockHttpResponse get() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + GitConfigResource.GIT_CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private MockHttpResponse put() throws URISyntaxException {
    JsonMockHttpRequest request = JsonMockHttpRequest.put("/" + GitConfigResource.GIT_CONFIG_PATH_V2)
                                             .contentType(GitVndMediaType.GIT_CONFIG)
                                             .json("{'disabled':true, 'defaultBranch':'main', 'lfsWriteAuthorizationExpirationInMinutes':5}");

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private GitConfig createConfiguration() {
    GitConfig config = new GitConfig();
    config.setGcExpression("valid Git GC Cron Expression");
    config.setDisabled(false);
    config.setLfsWriteAuthorizationExpirationInMinutes(5);
    return config;
  }
}
