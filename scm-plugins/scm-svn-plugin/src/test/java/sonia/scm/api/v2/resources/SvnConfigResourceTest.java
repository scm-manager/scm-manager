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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.SvnConfig;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.SvnVndMediaType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@SubjectAware(
  configuration = "classpath:sonia/scm/configuration/shiro.ini",
  password = "secret"
)
@RunWith(MockitoJUnitRunner.class)
public class SvnConfigResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private RestDispatcher dispatcher = new RestDispatcher();

  private final URI baseUri = URI.create("/");

  @Mock
  private RepositoryManager repositoryManager;

  @InjectMocks
  private SvnConfigDtoToSvnConfigMapperImpl dtoToConfigMapper;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  private SvnConfigToSvnConfigDtoMapperImpl configToDtoMapper;

  @Mock
  private SvnRepositoryHandler repositoryHandler;

  @Before
  public void prepareEnvironment() {
    SvnConfig gitConfig = createConfiguration();
    when(repositoryHandler.getConfig()).thenReturn(gitConfig);
    SvnConfigResource gitConfigResource = new SvnConfigResource(dtoToConfigMapper, configToDtoMapper, repositoryHandler);
    dispatcher.addSingletonResource(gitConfigResource);
    when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
  }

  @Test
  @SubjectAware(username = "readWrite")
  public void shouldGetSvnConfig() throws URISyntaxException, IOException {
    MockHttpResponse response = get();

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    String responseString = response.getContentAsString();
    ObjectNode responseJson = new ObjectMapper().readValue(responseString, ObjectNode.class);

    assertTrue(responseString.contains("\"disabled\":false"));
    assertTrue(responseString.contains("\"self\":{\"href\":\"/v2/config/svn"));
    assertTrue(responseString.contains("\"update\":{\"href\":\"/v2/config/svn"));
  }

  @Test
  @SubjectAware(username = "readWrite")
  public void shouldGetSvnConfigEvenWhenItsEmpty() throws URISyntaxException, IOException {
    when(repositoryHandler.getConfig()).thenReturn(null);

    MockHttpResponse response = get();
    String responseString = response.getContentAsString();

    assertTrue(responseString.contains("\"disabled\":false"));
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetSvnConfigWithoutUpdateLink() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpResponse response = get();

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    assertFalse(response.getContentAsString().contains("\"update\":{\"href\":\"/v2/config/svn"));
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldNotGetConfigWhenNotAuthorized() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpResponse response = get();

    assertEquals("Subject does not have permission [configuration:read:svn]", response.getContentAsString());
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

    assertEquals("Subject does not have permission [configuration:write:svn]", response.getContentAsString());
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  private MockHttpResponse get() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + SvnConfigResource.SVN_CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private MockHttpResponse put() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.put("/" + SvnConfigResource.SVN_CONFIG_PATH_V2)
                                             .contentType(SvnVndMediaType.SVN_CONFIG)
                                             .content("{\"disabled\":true}".getBytes());

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private SvnConfig createConfiguration() {
    SvnConfig config = new SvnConfig();
    config.setDisabled(false);
    return config;
  }

}

