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

import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.NotFoundException;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.RestDispatcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SourceRootResourceTest extends RepositoryTestBase {

  private final RestDispatcher dispatcher = new RestDispatcher();
  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private BrowseCommandBuilder browseCommandBuilder;

  @Before
  public void prepareEnvironment() {
    BrowserResultToFileObjectDtoMapper browserResultToFileObjectDtoMapper = Mappers.getMapper(BrowserResultToFileObjectDtoMapper.class);
    browserResultToFileObjectDtoMapper.setResourceLinks(resourceLinks);
    when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(service);
    when(service.getBrowseCommand()).thenReturn(browseCommandBuilder);

    sourceRootResource = new SourceRootResource(serviceFactory, browserResultToFileObjectDtoMapper);
    dispatcher.addSingletonResource(getRepositoryRootResource());
  }

  @Test
  public void shouldReturnSources() throws URISyntaxException, IOException {
    BrowserResult result = createBrowserResult();
    when(browseCommandBuilder.getBrowserResult()).thenReturn(result);
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/sources");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
    String content = response.getContentAsString();
    assertThat(content).contains("\"revision\":\"revision\"");
    assertThat(content).contains("\"children\":");
  }

  @Test
  public void shouldReturn404IfRepoNotFound() throws URISyntaxException {
    when(serviceFactory.create(new NamespaceAndName("idont", "exist"))).thenThrow(new NotFoundException("Test", "a"));
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "idont/exist/sources");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void shouldGetResultForSingleFile() throws URISyntaxException, IOException {
    FileObject fileObject = new FileObject();
    fileObject.setName("File Object!");
    fileObject.setPath("/");
    BrowserResult browserResult = new BrowserResult("revision", fileObject);

    when(browseCommandBuilder.getBrowserResult()).thenReturn(browserResult);
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/sources/revision/fileabc");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString()).contains("\"revision\":\"revision\"");
  }

  @Test
  public void shouldGet404ForSingleFileIfRepoNotFound() throws URISyntaxException {
    when(serviceFactory.create(new NamespaceAndName("idont", "exist"))).thenThrow(new NotFoundException("Test", "a"));

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "idont/exist/sources/revision/fileabc");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void collapseShouldBeFalseByDefault() throws Exception {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/sources");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(browseCommandBuilder).setCollapse(false);
  }

  @Test
  public void shouldSetCollapseToTrue() throws Exception {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/sources?collapse=true");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(browseCommandBuilder).setCollapse(true);
  }

  private BrowserResult createBrowserResult() {
    return new BrowserResult("revision", createFileObject());
  }

  private FileObject createFileObject() {
    FileObject parent = new FileObject();
    parent.setName("bar");
    parent.setPath("/foo/bar");
    parent.setDirectory(true);

    FileObject fileObject1 = new FileObject();
    fileObject1.setName("FO 1");
    fileObject1.setDirectory(false);
    fileObject1.setDescription("File object 1");
    fileObject1.setPath("/foo/bar/fo1");
    fileObject1.setLength(1024L);
    fileObject1.setCommitDate(0L);
    parent.addChild(fileObject1);

    FileObject fileObject2 = new FileObject();
    fileObject2.setName("FO 2");
    fileObject2.setDirectory(true);
    fileObject2.setDescription("File object 2");
    fileObject2.setPath("/foo/bar/fo2");
    fileObject2.setLength(4096L);
    fileObject2.setCommitDate(1234L);
    parent.addChild(fileObject2);

    return parent;
  }
}
