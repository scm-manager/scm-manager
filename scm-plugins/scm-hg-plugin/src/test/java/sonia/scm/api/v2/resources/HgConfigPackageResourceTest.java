/**
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

import com.fasterxml.jackson.databind.JsonNode;
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
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.installer.HgPackage;
import sonia.scm.installer.HgPackageReader;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.web.RestDispatcher;

import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static sonia.scm.api.v2.resources.HgConfigTests.createPackage;

@SubjectAware(
  configuration = "classpath:sonia/scm/configuration/shiro.ini",
  password = "secret"
)
@RunWith(MockitoJUnitRunner.class)
public class HgConfigPackageResourceTest {

  public static final String URI = "/" + HgConfigResource.HG_CONFIG_PATH_V2 + "/packages";
  @Rule
  public ShiroRule shiro = new ShiroRule();

  private RestDispatcher dispatcher = new RestDispatcher();

  private final URI baseUri = java.net.URI.create("/");

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  private HgConfigPackagesToDtoMapperImpl mapper;

  @Mock
  private HgRepositoryHandler repositoryHandler;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private HgPackageReader hgPackageReader;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private AdvancedHttpClient advancedHttpClient;

  @Mock
  private Provider<HgConfigPackageResource> hgConfigPackageResourceProvider;

  @Mock
  private HgPackage hgPackage;

  @Before
  public void prepareEnvironment() {
    setupResources();

    when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);

    when(hgPackageReader.getPackages().getPackages()).thenReturn(createPackages());
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetPackages() throws Exception {
    MockHttpResponse response = get();

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    String responseString = response.getContentAsString();
    ObjectNode responseJson = new ObjectMapper().readValue(responseString, ObjectNode.class);

    JsonNode packages = responseJson.get("packages");
    assertThat(packages).isNotNull();
    assertThat(packages).hasSize(2);

    JsonNode package1 = packages.get(0);
    assertThat(package1.get("_links")).isNull();

    JsonNode hgConfigTemplate = package1.get("hgConfigTemplate");
    assertThat(hgConfigTemplate).isNotNull();
    assertThat(hgConfigTemplate.get("_links")).isNull();

    assertThat(responseString).contains("\"_links\":{\"self\":{\"href\":\"/v2/config/hg/packages");
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldNotGetPackagesWhenNotAuthorized() throws Exception {
    MockHttpResponse response = get();

    assertEquals("Subject does not have permission [configuration:read:hg]", response.getContentAsString());
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldInstallPackage() throws Exception {
    String packgeId = "ourPackage";
    String url = "http://url";

    setupPackageInstallation(packgeId, url);
    when(advancedHttpClient.get(url).request().contentAsStream())
      .thenReturn(new ByteArrayInputStream("mockedFile".getBytes()));

    MockHttpResponse response = put(packgeId);
    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldHandleFailingInstallation() throws Exception {
    String packgeId = "ourPackage";
    String url = "http://url";

    setupPackageInstallation(packgeId, url);
    when(advancedHttpClient.get(url).request().contentAsStream())
      .thenThrow(new IOException("mocked Exception"));

    MockHttpResponse response = put(packgeId);
    assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldHandlePackagesThatAreNotFound() throws Exception {
    String packageId = "this-package-does-not-ex";
    when(hgPackageReader.getPackage(packageId)).thenReturn(null);
    MockHttpResponse response = put(packageId);
    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldNotInstallPackageWhenNotAuthorized() throws Exception {
    MockHttpResponse response = put("don-t-care");

    assertEquals("Subject does not have permission [configuration:write:hg]", response.getContentAsString());
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  private List<HgPackage> createPackages() {
    return Arrays.asList(createPackage(), new HgPackage());
  }

  private MockHttpResponse get() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get(URI);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private MockHttpResponse put(String pckgId) throws URISyntaxException {
    String packgeIdParam = "";
    if (pckgId != null) {
      packgeIdParam = "/" + pckgId;
    }
    MockHttpRequest request = MockHttpRequest.put(URI + packgeIdParam);

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private void setupResources() {
    HgConfigPackageResource hgConfigPackageResource =
      new HgConfigPackageResource(hgPackageReader, advancedHttpClient, repositoryHandler, mapper);

    when(hgConfigPackageResourceProvider.get()).thenReturn(hgConfigPackageResource);
    dispatcher.addSingletonResource(
      new HgConfigResource(null, null, null,
                           hgConfigPackageResourceProvider, null, null));
  }

  private void setupPackageInstallation(String packgeId, String url) throws IOException {
    when(hgPackage.getId()).thenReturn(packgeId);
    when(hgPackageReader.getPackage(packgeId)).thenReturn(hgPackage);
    when(repositoryHandler.getConfig()).thenReturn(new HgConfig());
    when(hgPackage.getHgConfigTemplate()).thenReturn(new HgConfig());
    when(hgPackage.getUrl()).thenReturn(url);
  }

}
