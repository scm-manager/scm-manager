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

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class AdvancedHttpClientTest {

  @Mock(answer = Answers.CALLS_REAL_METHODS)
  private AdvancedHttpClient client;

  private static final String URL = "https://www.scm-manager.org";
  
  @Test
  public void testGet()
  {
    AdvancedHttpRequest request = client.get(URL);
    assertEquals(URL, request.getUrl());
    assertEquals(HttpMethod.GET, request.getMethod());
  }
  
  @Test
  public void testDelete()
  {
    AdvancedHttpRequestWithBody request = client.delete(URL);
    assertEquals(URL, request.getUrl());
    assertEquals(HttpMethod.DELETE, request.getMethod());
  }
  
  @Test
  public void testPut()
  {
    AdvancedHttpRequestWithBody request = client.put(URL);
    assertEquals(URL, request.getUrl());
    assertEquals(HttpMethod.PUT, request.getMethod());
  }
  
  @Test
  public void testPost()
  {
    AdvancedHttpRequestWithBody request = client.post(URL);
    assertEquals(URL, request.getUrl());
    assertEquals(HttpMethod.POST, request.getMethod());
  }
  
  @Test
  public void testOptions()
  {
    AdvancedHttpRequestWithBody request = client.options(URL);
    assertEquals(URL, request.getUrl());
    assertEquals(HttpMethod.OPTIONS, request.getMethod());
  }
  
  @Test
  public void testHead()
  {
    AdvancedHttpRequest request = client.head(URL);
    assertEquals(URL, request.getUrl());
    assertEquals(HttpMethod.HEAD, request.getMethod());
  }
  
  @Test
  public void testMethod()
  {
    AdvancedHttpRequestWithBody request = client.method("PROPFIND", URL);
    assertEquals(URL, request.getUrl());
    assertEquals("PROPFIND", request.getMethod());
  }
}
