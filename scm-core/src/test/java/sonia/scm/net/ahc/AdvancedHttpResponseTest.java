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
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class AdvancedHttpResponseTest {

  @Mock(answer = Answers.CALLS_REAL_METHODS)
  private AdvancedHttpResponse response;
  
  @Mock
  private ContentTransformer transformer;

  @Test
  public void testContent() throws IOException
  {
    ByteSource bs = ByteSource.wrap("test123".getBytes(Charsets.UTF_8));
    when(response.contentAsByteSource()).thenReturn(bs);
    byte[] data = response.content();
    assertEquals("test123", new String(data, Charsets.UTF_8));
  }
  
  @Test
  public void testContentWithoutByteSource() throws IOException
  {
    assertNull(response.content());
  }
  
  @Test
  public void testContentAsString() throws IOException
  {
    ByteSource bs = ByteSource.wrap("123test".getBytes(Charsets.UTF_8));
    when(response.contentAsByteSource()).thenReturn(bs);
    assertEquals("123test", response.contentAsString());
  }
  
  @Test
  public void testContentAsStingWithoutByteSource() throws IOException 
  {
    assertNull(response.contentAsString());
  }
  
  @Test
  public void testContentAsReader() throws IOException
  {
    ByteSource bs = ByteSource.wrap("abc123".getBytes(Charsets.UTF_8));
    when(response.contentAsByteSource()).thenReturn(bs);
    assertEquals("abc123", CharStreams.toString(response.contentAsReader()));
  }
  
  @Test
  public void testContentAsReaderWithoutByteSource() throws IOException
  {
    assertNull(response.contentAsReader());
  }
  
  @Test
  public void testContentAsStream() throws IOException
  {
    ByteSource bs = ByteSource.wrap("cde456".getBytes(Charsets.UTF_8));
    when(response.contentAsByteSource()).thenReturn(bs);
    byte[] data = ByteStreams.toByteArray(response.contentAsStream());
    assertEquals("cde456", new String(data, Charsets.UTF_8));
  }
  
  @Test
  public void testContentAsStreamWithoutByteSource() throws IOException
  {
    assertNull(response.contentAsStream());
  }
  
  @Test
  public void testContentFromJson() throws IOException{
    ByteSource bs = ByteSource.wrap("{}".getBytes(Charsets.UTF_8));
    when(response.contentAsByteSource()).thenReturn(bs);
    when(response.createTransformer(String.class, ContentType.JSON)).thenReturn(transformer);
    when(transformer.unmarshall(String.class, bs)).thenReturn("{root: null}");
    String c = response.contentFromJson(String.class);
    assertEquals("{root: null}", c);
  }
  
  @Test
  public void testContentFromXml() throws IOException{
    ByteSource bs = ByteSource.wrap("<root />".getBytes(Charsets.UTF_8));
    when(response.contentAsByteSource()).thenReturn(bs);
    when(response.createTransformer(String.class, ContentType.XML)).thenReturn(transformer);
    when(transformer.unmarshall(String.class, bs)).thenReturn("<root></root>");
    String c = response.contentFromXml(String.class);
    assertEquals("<root></root>", c);
  }
  
  @Test(expected = ContentTransformerException.class)
  public void testContentTransformedWithoutHeader() throws IOException{
    Multimap<String,String> map = LinkedHashMultimap.create();
    when(response.getHeaders()).thenReturn(map);
    response.contentTransformed(String.class);
  }
  
  @Test
  public void testContentTransformedFromHeader() throws IOException{
    Multimap<String,String> map = LinkedHashMultimap.create();
    map.put("Content-Type", "text/plain");
    when(response.getHeaders()).thenReturn(map);
    when(response.createTransformer(String.class, "text/plain")).thenReturn(
      transformer);
    ByteSource bs = ByteSource.wrap("hello".getBytes(Charsets.UTF_8));
    when(response.contentAsByteSource()).thenReturn(bs);
    when(transformer.unmarshall(String.class, bs)).thenReturn("hello world");
    String v = response.contentTransformed(String.class);
    assertEquals("hello world", v);
  }
  
  @Test
  public void testContentTransformed() throws IOException{
    when(response.createTransformer(String.class, "text/plain")).thenReturn(
      transformer);
    ByteSource bs = ByteSource.wrap("hello".getBytes(Charsets.UTF_8));
    when(response.contentAsByteSource()).thenReturn(bs);
    when(transformer.unmarshall(String.class, bs)).thenReturn("hello world");
    String v = response.contentTransformed(String.class, "text/plain");
    assertEquals("hello world", v);
  }
  
  @Test
  public void testContentTransformedWithoutByteSource() throws IOException{
    assertNull(response.contentTransformed(String.class, "text/plain"));
  }
  
  @Test
  public void testGetFirstHeader() throws IOException
  {
    Multimap<String,String> mm = LinkedHashMultimap.create();
    mm.put("Test", "One");
    mm.put("Test-2", "One");
    mm.put("Test-2", "Two");
    when(response.getHeaders()).thenReturn(mm);
    assertEquals("One", response.getFirstHeader("Test"));
    assertEquals("One", response.getFirstHeader("Test-2"));
    assertNull(response.getFirstHeader("Test-3"));
  }
  
  @Test
  public void testIsSuccessful() throws IOException
  {
    // successful
    when(response.getStatus()).thenReturn(200);
    assertTrue(response.isSuccessful());
    when(response.getStatus()).thenReturn(201);
    assertTrue(response.isSuccessful());
    when(response.getStatus()).thenReturn(204);
    assertTrue(response.isSuccessful());
    when(response.getStatus()).thenReturn(301);
    assertTrue(response.isSuccessful());
    
    // not successful
    when(response.getStatus()).thenReturn(400);
    assertFalse(response.isSuccessful());
    when(response.getStatus()).thenReturn(404);
    assertFalse(response.isSuccessful());
    when(response.getStatus()).thenReturn(500);
    assertFalse(response.isSuccessful());
    when(response.getStatus()).thenReturn(199);
    assertFalse(response.isSuccessful());
  }

}