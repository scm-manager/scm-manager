/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * http://bitbucket.org/sdorra/scm-manager
 * 
 */

package sonia.scm.net.ahc;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

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
