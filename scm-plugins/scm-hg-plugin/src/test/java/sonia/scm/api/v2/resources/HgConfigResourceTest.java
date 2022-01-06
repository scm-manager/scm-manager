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
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.HgVndMediaType;
import sonia.scm.web.RestDispatcher;

import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@SubjectAware(
  configuration = "classpath:sonia/scm/configuration/shiro.ini",
  password = "secret"
)
@RunWith(MockitoJUnitRunner.class)
public class HgConfigResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private final RestDispatcher dispatcher = new RestDispatcher();

  @Mock
  private RepositoryManager repositoryManager;

  @InjectMocks
  private HgGlobalConfigDtoToHgConfigMapperImpl dtoToConfigMapper;

  @Mock
  private HgRepositoryHandler repositoryHandler;

  @Mock
  private Provider<HgGlobalConfigAutoConfigurationResource> autoconfigResource;

  @Mock
  private Provider<HgRepositoryConfigResource> repositoryConfigResource;

  @Before
  public void prepareEnvironment() {
    HgGlobalConfig gitConfig = createConfiguration();
    when(repositoryHandler.getConfig()).thenReturn(gitConfig);
    when(repositoryManager.getAll()).thenReturn(Collections.singleton(RepositoryTestData.create42Puzzle()));

    HgConfigResource gitConfigResource = new HgConfigResource(
      dtoToConfigMapper, createConfigToDtoMapper(), repositoryHandler,
      autoconfigResource, repositoryConfigResource
    );
    dispatcher.addSingletonResource(gitConfigResource);
  }

  private HgGlobalConfigToHgGlobalConfigDtoMapper createConfigToDtoMapper() {
    ScmPathInfoStore store = new ScmPathInfoStore();
    store.set(() -> URI.create("/"));
    HgConfigLinks links = new HgConfigLinks(store);
    HgGlobalConfigToHgGlobalConfigDtoMapper mapper = Mappers.getMapper(
      HgGlobalConfigToHgGlobalConfigDtoMapper.class
    );
    mapper.setLinks(links);
    mapper.setRepositoryManager(repositoryManager);
    return mapper;
  }

  @Test
  @SubjectAware(username = "readWrite")
  public void shouldGetHgConfig() throws URISyntaxException, IOException {
    MockHttpResponse response = get();

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    String responseString = response.getContentAsString();
    ObjectNode responseJson = new ObjectMapper().readValue(responseString, ObjectNode.class);

    assertTrue(responseString.contains("\"disabled\":false"));
    assertTrue(responseString.contains("\"self\":{\"href\":\"/v2/config/hg"));
    assertTrue(responseString.contains("\"update\":{\"href\":\"/v2/config/hg"));
  }

  @Test
  @SubjectAware(username = "readWrite")
  public void shouldGetHgConfigEvenWhenItsEmpty() throws URISyntaxException, UnsupportedEncodingException {
    when(repositoryHandler.getConfig()).thenReturn(null);

    MockHttpResponse response = get();
    String responseString = response.getContentAsString();

    assertTrue(responseString.contains("\"disabled\":false"));
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetHgConfigWithoutUpdateLink() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpResponse response = get();

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    assertFalse(response.getContentAsString().contains("\"update\":{\"href\":\"/v2/config/hg"));
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldNotGetConfigWhenNotAuthorized() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpResponse response = get();

    assertEquals("Subject does not have permission [configuration:read:hg]", response.getContentAsString());
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldUpdateConfig() throws URISyntaxException {
    MockHttpResponse response = put();
    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldNotUpdateConfigForInvalidBinary() throws URISyntaxException {
    MockHttpResponse response = put("{\"hgBinary\":\"3.2.1\"}");
    assertEquals(400, response.getStatus());
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldNotUpdateConfigWhenNotAuthorized() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpResponse response = put();

    assertEquals("Subject does not have permission [configuration:write:hg]", response.getContentAsString());
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  private MockHttpResponse get() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + HgConfigResource.HG_CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private MockHttpResponse put() throws URISyntaxException {
    return put("{\"disabled\":true}");
  }

  private MockHttpResponse put(String config) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.put("/" + HgConfigResource.HG_CONFIG_PATH_V2)
                                             .contentType(HgVndMediaType.CONFIG)
                                             .content(config.getBytes());

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private HgGlobalConfig createConfiguration() {
    HgGlobalConfig config = new HgGlobalConfig();
    config.setDisabled(false);
    return config;
  }

}

