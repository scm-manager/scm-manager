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
    
package sonia.scm.net.ahc;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AdvancedHttpRequestWithBodyTest {

  @Mock
  private AdvancedHttpClient ahc;
  
  @Mock
  private ContentTransformer transformer;
  
  private AdvancedHttpRequestWithBody request;

  @BeforeEach
  void before(){
    request = new AdvancedHttpRequestWithBody(ahc, HttpMethod.PUT, "https://www.scm-manager.org");
  }

  @Test
  void shouldReturnContentLength() {
    request.contentLength(12L);
    assertThat(request.getHeaders().get("Content-Length").iterator().next()).isEqualTo("12");
  }
  
  @Test
  void shouldReturnContentType(){
    request.contentType("text/plain");
    assertThat(request.getHeaders().get("Content-Type").iterator().next()).isEqualTo("text/plain");
  }
  
  @Test
  void shouldReturnFileContent(@TempDir Path path) {
    request.fileContent(path.toFile());
    assertThat(request.getContent()).isInstanceOf(FileContent.class);
  }

  @Test
  void shouldReturnRawContent() {
    request.rawContent("test".getBytes(Charsets.UTF_8));
    assertThat(request.getContent()).isInstanceOf(RawContent.class);
  }
  
  @Test
  void shouldReturnRawContentFromByteSource() {
    ByteSource bs = ByteSource.wrap("test".getBytes(Charsets.UTF_8));
    request.rawContent(bs);
    assertThat(request.getContent()).isInstanceOf(ByteSourceContent.class);
  }
  
  @Test
  void shouldApplyFormContent(){
    FormContentBuilder builder = request.formContent();
    assertThat(builder).isNotNull();
    builder.build();
    assertThat(request.getContent()).isInstanceOf(StringContent.class);
  }
  
  @Test
  void shouldReturnStringContent(){
    request.stringContent("test");
    assertThat(request.getContent()).isInstanceOf(StringContent.class);
  }
  
  @Test
  void shouldReturnStringContentFormStringContentWithCharset(){
    request.stringContent("test", Charsets.UTF_8);
    assertThat(request.getContent()).isInstanceOf(StringContent.class);
  }

  @Test
  void shouldReturnCustomContent(){
    request.content(new CustomContent());
    assertThat(request.getContent()).isInstanceOf(CustomContent.class);
  }
  
  @Test
  void shouldReturnXmlContent() throws IOException {
    when(ahc.createTransformer(String.class, ContentType.XML)).thenReturn(transformer);
    when(transformer.marshall("<root />")).thenReturn(ByteSource.wrap("<root></root>".getBytes(Charsets.UTF_8)));

    Content content = request.xmlContent("<root />").getContent();
    assertThat(content).isInstanceOf(ByteSourceContent.class);

    ByteSourceContent bsc = (ByteSourceContent) content;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bsc.process(baos);

    assertThat(baos.toString("UTF-8")).isEqualTo("<root></root>");
  }
  
  @Test
  void shouldReturnJsonContent() throws IOException{
    when(ahc.createTransformer(String.class, ContentType.JSON)).thenReturn(transformer);
    when(transformer.marshall("{}")).thenReturn(ByteSource.wrap("{'root': {}}".getBytes(Charsets.UTF_8)));

    Content content = request.jsonContent("{}").getContent();
    assertThat(content).isInstanceOf(ByteSourceContent.class);

    ByteSourceContent bsc = (ByteSourceContent) content;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bsc.process(baos);

    assertThat(baos.toString("UTF-8")).isEqualTo("{'root': {}}");
  }
  
  @Test
  void shouldReturnTransformedContent() throws IOException{
    when(ahc.createTransformer(String.class, "text/plain")).thenReturn(transformer);
    when(transformer.marshall("hello")).thenReturn(ByteSource.wrap("hello world".getBytes(Charsets.UTF_8)));

    Content content = request.transformedContent("text/plain", "hello").getContent();
    assertThat(content).isInstanceOf(ByteSourceContent.class);

    ByteSourceContent bsc = (ByteSourceContent) content;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bsc.process(baos);

   assertThat(baos.toString("UTF-8")).isEqualTo("hello world");
  }
  
  @Test
  void shouldReturnSelf() {
    assertThat(request.self().getClass()).isEqualTo(AdvancedHttpRequestWithBody.class);
  }

  private static class CustomContent implements Content {
    @Override
    public void prepare(AdvancedHttpRequestWithBody request) throws IOException {

    }

    @Override
    public void process(OutputStream output) throws IOException {

    }
  }
  
}
