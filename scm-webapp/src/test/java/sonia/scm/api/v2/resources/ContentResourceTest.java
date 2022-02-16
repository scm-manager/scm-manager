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

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.NotFoundException;
import sonia.scm.io.DefaultContentTypeResolver;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.CatCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentResourceTest {

  private static final String NAMESPACE = "space";
  private static final String REPO_NAME = "name";
  private static final String REV = "rev";

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryServiceFactory repositoryServiceFactory;

  private ContentResource contentResource;

  private CatCommandBuilder catCommand;

  @Before
  public void initService() throws Exception {
    contentResource = new ContentResource(repositoryServiceFactory, new DefaultContentTypeResolver());

    NamespaceAndName existingNamespaceAndName = new NamespaceAndName(NAMESPACE, REPO_NAME);
    RepositoryService repositoryService = repositoryServiceFactory.create(existingNamespaceAndName);
    catCommand = repositoryService.getCatCommand();
    when(catCommand.setRevision(REV)).thenReturn(catCommand);

    // defaults for unknown things
    doThrow(new NotFoundException("Test", "r")).when(repositoryServiceFactory).create(not(eq(existingNamespaceAndName)));
    doThrow(new NotFoundException("Test", "X")).when(catCommand).getStream(any());
  }

  @Test
  public void shouldReadSimpleFile() throws Exception {
    mockContent("file", "Hello".getBytes());

    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "file", null, null);
    assertEquals(200, response.getStatus());

    ByteArrayOutputStream baos = readOutputStream(response);

    assertEquals("Hello", baos.toString());
  }

  @Test
  public void shouldLimitOutputByLines() throws Exception {
    mockContent("file", "line 1\nline 2\nline 3\nline 4".getBytes());

    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "file", 1, 3);
    assertEquals(200, response.getStatus());

    ByteArrayOutputStream baos = readOutputStream(response);

    assertEquals("line 2\nline 3\n", baos.toString());
  }

  @Test
  public void shouldNotLimitOutputWhenEndLessThanZero() throws Exception {
    mockContent("file", "line 1\nline 2\nline 3\nline 4".getBytes());

    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "file", 1, -1);
    assertEquals(200, response.getStatus());

    ByteArrayOutputStream baos = readOutputStream(response);

    assertEquals("line 2\nline 3\nline 4", baos.toString());
  }

  @Test
  public void shouldHandleMissingFile() {
    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "doesNotExist", null, null);
    assertEquals(404, response.getStatus());
  }

  @Test
  public void shouldHandleMissingRepository() {
    Response response = contentResource.get("no", "repo", REV, "anything", null, null);
    assertEquals(404, response.getStatus());
  }

  @Test
  public void shouldRecognizeTikaSourceCode() throws Exception {
    mockContentFromResource("SomeGoCode.go");

    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "SomeGoCode.go", null, null);
    assertEquals(200, response.getStatus());

    assertEquals("Go", response.getHeaderString("X-Programming-Language"));
    assertEquals("text/x-go", response.getHeaderString("Content-Type"));
  }

  @Test
  public void shouldRecognizeSpecialSourceCode() throws Exception {
    mockContentFromResource("Dockerfile");

    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "Dockerfile", null, null);
    assertEquals(200, response.getStatus());

    assertEquals("Dockerfile", response.getHeaderString("X-Programming-Language"));
    assertEquals("text/plain", response.getHeaderString("Content-Type"));
  }

  @Test
  public void shouldRecognizeSyntaxModes() throws Exception {
    mockContentFromResource("SomeGoCode.go");

    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "SomeGoCode.go", null, null);
    assertEquals(200, response.getStatus());

    assertEquals("golang", response.getHeaderString("X-Syntax-Mode-Ace"));
    assertEquals("go", response.getHeaderString("X-Syntax-Mode-Codemirror"));
    assertEquals("go", response.getHeaderString("X-Syntax-Mode-Prism"));
  }

  @Test
  public void shouldHandleRandomByteFile() throws Exception {
    mockContentFromResource("JustBytes");

    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "JustBytes", null, null);
    assertEquals(200, response.getStatus());

    assertFalse(response.getHeaders().containsKey("Language"));
    assertEquals("application/octet-stream", response.getHeaderString("Content-Type"));
  }

  @Test
  public void shouldNotReadCompleteFileForHead() throws Exception {
    FailingAfterSomeBytesStream stream = new FailingAfterSomeBytesStream();
    doAnswer(invocation -> stream).when(catCommand).getStream("readHeadOnly");

    Response response = contentResource.metadata(NAMESPACE, REPO_NAME, REV, "readHeadOnly");
    assertEquals(200, response.getStatus());

    assertEquals("application/octet-stream", response.getHeaderString("Content-Type"));
    assertTrue("stream has to be closed after reading head", stream.isClosed());
  }

  @Test
  public void shouldHandleEmptyFile() throws Exception {
    mockContent("empty", new byte[]{});

    Response response = contentResource.get(NAMESPACE, REPO_NAME, REV, "empty", null, null);
    assertEquals(200, response.getStatus());

    assertFalse(response.getHeaders().containsKey("Language"));
    assertEquals("application/octet-stream", response.getHeaderString("Content-Type"));
  }

  @SuppressWarnings("UnstableApiUsage")
  private void mockContentFromResource(String fileName) throws Exception {
    URL url = Resources.getResource(fileName);
    mockContent(fileName, Resources.toByteArray(url));
  }

  private void mockContent(String path, byte[] content) throws Exception {
    doAnswer(invocation -> {
      OutputStream outputStream = (OutputStream) invocation.getArguments()[0];
      outputStream.write(content);
      outputStream.close();
      return null;
    }).when(catCommand).retriveContent(any(), eq(path));
    doAnswer(invocation -> new ByteArrayInputStream(content)).when(catCommand).getStream(path);
  }

  private ByteArrayOutputStream readOutputStream(Response response) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ((StreamingOutput) response.getEntity()).write(baos);
    return baos;
  }

  private static class FailingAfterSomeBytesStream extends InputStream {
    private int bytesRead = 0;
    private boolean closed = false;
    @Override
    public int read() {
      if (++bytesRead > 1024) {
        fail("read too many bytes");
      }
      return 0;
    }

    @Override
    public void close() {
      closed = true;
    }

    public boolean isClosed() {
      return closed;
    }
  }
}
