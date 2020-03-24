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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.web.RestDispatcher;

import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@SubjectAware(
  configuration = "classpath:sonia/scm/configuration/shiro.ini",
  password = "secret"
)
@RunWith(MockitoJUnitRunner.class)
public class HgConfigInstallationsResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private RestDispatcher dispatcher = new RestDispatcher();

  private final URI baseUri = URI.create("/");

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  private HgConfigInstallationsToDtoMapper mapper;

  @Mock
  private Provider<HgConfigInstallationsResource> resourceProvider;


  @Before
  public void prepareEnvironment() {
    HgConfigInstallationsResource resource = new HgConfigInstallationsResource(mapper);

    when(resourceProvider.get()).thenReturn(resource);
    dispatcher.addSingletonResource(
      new HgConfigResource(null, null, null, null,
                           null, resourceProvider));

    when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetHgInstallations() throws Exception {
    MockHttpResponse response = get("hg");

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    String contentAsString = response.getContentAsString();
    assertThat(contentAsString).contains("{\"paths\":[");
    assertThat(contentAsString).contains("hg");
    assertThat(contentAsString).doesNotContain("python");

    assertThat(contentAsString).contains("\"self\":{\"href\":\"/v2/config/hg/installations/hg");
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldNotGetHgInstallationsWhenNotAuthorized() throws Exception {
    MockHttpResponse response = get("hg");

    assertEquals("Subject does not have permission [configuration:read:hg]", response.getContentAsString());
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetPythonInstallations() throws Exception {
    MockHttpResponse response = get("python");

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    String contentAsString = response.getContentAsString();
    assertThat(contentAsString).contains("{\"paths\":[");
    assertThat(contentAsString).contains("python");

    assertThat(contentAsString).contains("\"self\":{\"href\":\"/v2/config/hg/installations/python");
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldNotGetPythonInstallationsWhenNotAuthorized() throws Exception {
    MockHttpResponse response = get("python");

    assertEquals("Subject does not have permission [configuration:read:hg]", response.getContentAsString());
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  private MockHttpResponse get(String path) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + HgConfigResource.HG_CONFIG_PATH_V2 + "/installations/" + path);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }
}
