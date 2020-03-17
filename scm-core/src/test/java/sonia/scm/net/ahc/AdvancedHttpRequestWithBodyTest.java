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
package sonia.scm.net.ahc;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class AdvancedHttpRequestWithBodyTest {

  @Mock
  private AdvancedHttpClient ahc;
  
  @Mock
  private ContentTransformer transformer;
  
  private AdvancedHttpRequestWithBody request;
  
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
  
  @Before
  public void before(){
    request = new AdvancedHttpRequestWithBody(ahc, HttpMethod.PUT, "https://www.scm-manager.org");
  }

  @Test
  public void testContentLength()
  {
    request.contentLength(12l);
    assertEquals("12", request.getHeaders().get("Content-Length").iterator().next());
  }
  
  @Test
  public void testContentType(){
    request.contentType("text/plain");
    assertEquals("text/plain", request.getHeaders().get("Content-Type").iterator().next());
  }
  
  @Test
  public void testFileContent() throws IOException{
    File file = tempFolder.newFile();
    request.fileContent(file);
    assertThat(request.getContent(), instanceOf(FileContent.class));
  }

  @Test
  public void testRawContent() throws IOException {
    request.rawContent("test".getBytes(Charsets.UTF_8));
    assertThat(request.getContent(), instanceOf(RawContent.class));
  }
  
  @Test
  public void testRawContentWithByteSource() throws IOException {
    ByteSource bs = ByteSource.wrap("test".getBytes(Charsets.UTF_8));
    request.rawContent(bs);
    assertThat(request.getContent(), instanceOf(ByteSourceContent.class));
  }
  
  @Test
  public void testFormContent(){
    FormContentBuilder builder = request.formContent();
    assertNotNull(builder);
    builder.build();
    assertThat(request.getContent(), instanceOf(StringContent.class));
  }
  
  @Test
  public void testStringContent(){
    request.stringContent("test");
    assertThat(request.getContent(), instanceOf(StringContent.class));
  }
  
  @Test
  public void testStringContentWithCharset(){
    request.stringContent("test", Charsets.UTF_8);
    assertThat(request.getContent(), instanceOf(StringContent.class));
  }
  
  @Test
  public void testXmlContent() throws IOException{
    when(ahc.createTransformer(String.class, ContentType.XML)).thenReturn(transformer);
    when(transformer.marshall("<root />")).thenReturn(ByteSource.wrap("<root></root>".getBytes(Charsets.UTF_8)));
    Content content = request.xmlContent("<root />").getContent();
    assertThat(content, instanceOf(ByteSourceContent.class));
    ByteSourceContent bsc = (ByteSourceContent) content;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bsc.process(baos);
    assertEquals("<root></root>", baos.toString("UTF-8"));
  }
  
  @Test
  public void testJsonContent() throws IOException{
    when(ahc.createTransformer(String.class, ContentType.JSON)).thenReturn(transformer);
    when(transformer.marshall("{}")).thenReturn(ByteSource.wrap("{'root': {}}".getBytes(Charsets.UTF_8)));
    Content content = request.jsonContent("{}").getContent();
    assertThat(content, instanceOf(ByteSourceContent.class));
    ByteSourceContent bsc = (ByteSourceContent) content;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bsc.process(baos);
    assertEquals("{'root': {}}", baos.toString("UTF-8"));
  }
  
 @Test
  public void testTransformedContent() throws IOException{
    when(ahc.createTransformer(String.class, "text/plain")).thenReturn(transformer);
    when(transformer.marshall("hello")).thenReturn(ByteSource.wrap("hello world".getBytes(Charsets.UTF_8)));
    Content content = request.transformedContent("text/plain", "hello").getContent();
    assertThat(content, instanceOf(ByteSourceContent.class));
    ByteSourceContent bsc = (ByteSourceContent) content;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bsc.process(baos);
    assertEquals("hello world", baos.toString("UTF-8"));
  }
  
  @Test
  public void testSelf()
  {
    assertEquals(AdvancedHttpRequestWithBody.class, request.self().getClass());
  }
  
}
