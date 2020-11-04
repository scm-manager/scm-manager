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
import sonia.scm.web.RestDispatcher;

import javax.servlet.http.HttpServletResponse;
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

  private RestDispatcher dispatcher = new RestDispatcher();

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
    GitRepositoryConfigResource gitRepositoryConfigResource = new GitRepositoryConfigResource(repositoryConfigMapper, repositoryManager, new GitRepositoryConfigStoreProvider(configurationStoreFactory));
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

    assertTrue(responseString.contains("\"disabled\":false"));
    assertTrue(responseString.contains("\"gcExpression\":\"valid Git GC Cron Expression\""));
    assertTrue(responseString.contains("\"self\":{\"href\":\"/v2/config/git"));
    assertTrue(responseString.contains("\"update\":{\"href\":\"/v2/config/git"));
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

  private MockHttpResponse get() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + GitConfigResource.GIT_CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private MockHttpResponse put() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.put("/" + GitConfigResource.GIT_CONFIG_PATH_V2)
                                             .contentType(GitVndMediaType.GIT_CONFIG)
                                             .content("{\"disabled\":true, \"defaultBranch\":\"main\"}".getBytes());

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private GitConfig createConfiguration() {
    GitConfig config = new GitConfig();
    config.setGcExpression("valid Git GC Cron Expression");
    config.setDisabled(false);
    return config;
  }
}
