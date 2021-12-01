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

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

public class BrowserResultToFileObjectDtoMapperTest {

  private final URI baseUri = URI.create("http://example.com/base/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  private BrowserResultToFileObjectDtoMapper mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private FileObject fileObject1 = new FileObject();
  private FileObject fileObject2 = new FileObject();


  @Before
  public void init() {
    initMocks(this);
    mapper = Mappers.getMapper(BrowserResultToFileObjectDtoMapper.class);
    mapper.setResourceLinks(resourceLinks);

    subjectThreadState.bind();
    ThreadContext.bind(subject);

    fileObject1.setName("FO 1");
    fileObject1.setLength(100L);
    fileObject1.setCommitDate(0L);
    fileObject1.setPath("/path/object/1");
    fileObject1.setDescription("description of file object 1");
    fileObject1.setDirectory(false);

    fileObject2.setName("FO 2");
    fileObject2.setLength(100L);
    fileObject2.setCommitDate(101L);
    fileObject2.setPath("/path/object/2");
    fileObject2.setDescription("description of file object 2");
    fileObject2.setDirectory(true);
  }

  @After
  public void unbind() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldMapAttributesCorrectly() {
    BrowserResult browserResult = createBrowserResult();

    FileObjectDto dto = mapper.map(browserResult, new NamespaceAndName("foo", "bar"), 0);

    assertEqualAttributes(browserResult, dto);
  }

  @Test
  public void shouldDelegateToFileObjectsMapper() {
    BrowserResult browserResult = createBrowserResult();
    NamespaceAndName namespaceAndName = new NamespaceAndName("foo", "bar");

    FileObjectDto dto = mapper.map(browserResult, namespaceAndName, 0);

    assertThat(dto.getEmbedded().getItemsBy("children")).hasSize(2);
  }

  @Test
  public void shouldSetLinksCorrectly() {
    BrowserResult browserResult = createBrowserResult();
    NamespaceAndName namespaceAndName = new NamespaceAndName("foo", "bar");

    FileObjectDto dto = mapper.map(browserResult, namespaceAndName, 0);

    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).contains("path");
  }

  @Test
  public void shouldEncodeFileLinks() {
    BrowserResult browserResult = new BrowserResult("Revision", createFileObject("c:file"));
    NamespaceAndName namespaceAndName = new NamespaceAndName("foo", "bar");

    FileObjectDto dto = mapper.map(browserResult, namespaceAndName, 0);

    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("http://example.com/base/v2/repositories/foo/bar/content/Revision/c:file");
  }

  private BrowserResult createBrowserResult() {
    return new BrowserResult("Revision", createDirectoryObject());
  }

  private FileObject createDirectoryObject() {
    FileObject directory = new FileObject();
    directory.setName("");
    directory.setPath("/path");
    directory.setDirectory(true);

    directory.addChild(fileObject1);
    directory.addChild(fileObject2);
    return directory;
  }

  private FileObject createFileObject(String name) {
    FileObject file = new FileObject();
    file.setName("");
    file.setPath(name);
    file.setDirectory(false);
    return file;
  }

  private void assertEqualAttributes(BrowserResult browserResult, FileObjectDto dto) {
    assertThat(dto.getRevision()).isEqualTo(browserResult.getRevision());
  }

}
