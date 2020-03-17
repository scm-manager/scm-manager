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

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class BaseHttpRequestTest {

  @Mock
  private AdvancedHttpClient ahc;
  
  private BaseHttpRequest<AdvancedHttpRequest> request;
  
  @Before
  public void before(){
    request = new AdvancedHttpRequest(ahc, HttpMethod.GET, "https://www.scm-manager.org");
  }

  @Test
  public void testBasicAuth()
  {
    request.basicAuth("tricia", "mcmillian123");
    Multimap<String,String> headers = request.getHeaders();
    assertEquals("Basic dHJpY2lhOm1jbWlsbGlhbjEyMw==", headers.get("Authorization").iterator().next());
  }
  
  @Test
  public void testQueryString(){
    request.queryString("a", "b");
    assertEquals("https://www.scm-manager.org?a=b", request.getUrl());
  }
  
  @Test
  public void testQueryStringMultiple(){
    request.queryString("a", "b");
    request.queryString("c", "d", "e");
    assertEquals("https://www.scm-manager.org?a=b&c=d&c=e", request.getUrl());
  }
  
  @Test
  public void testQueryStringEncoded(){
    request.queryString("a", "äüö");
    assertEquals("https://www.scm-manager.org?a=%C3%A4%C3%BC%C3%B6", request.getUrl());
  }
  
  @Test
  public void testQueryStrings(){
    Iterable<? extends Object> i1 = Lists.newArrayList("b");
    Iterable<? extends Object> i2 = Lists.newArrayList("d", "e");
    request.queryStrings("a", i1);
    request.queryStrings("c", i2);
    assertEquals("https://www.scm-manager.org?a=b&c=d&c=e", request.getUrl());
  }
  
  @Test
  public void testQuerqStringNullValue(){
    request.queryString("a", null, "b");
    assertEquals("https://www.scm-manager.org?a=&a=b", request.getUrl());
  }
  
  @Test
  public void testHeader(){
    request.header("a", "b");
    assertEquals("b", request.getHeaders().get("a").iterator().next());
  }
  
  @Test
  public void testHeaderMultiple(){
    request.header("a", "b", "c", "d");
    Collection<String> values = request.getHeaders().get("a");
    assertThat(values, contains("b", "c", "d"));
  }
  
  @Test
  public void testRequest() throws IOException{
    request.request();
    verify(ahc).request(request);
  }
  
  @Test
  public void testBuilderMethods(){
    Iterable<? extends Object> i1 = Lists.newArrayList("b");
    assertThat(request.decodeGZip(true), instanceOf(AdvancedHttpRequest.class));
    assertTrue(request.isDecodeGZip());
    assertThat(request.disableCertificateValidation(true), instanceOf(AdvancedHttpRequest.class));
    assertTrue(request.isDisableCertificateValidation());
    assertThat(request.disableHostnameValidation(true), instanceOf(AdvancedHttpRequest.class));
    assertTrue(request.isDisableHostnameValidation());
    assertThat(request.ignoreProxySettings(true), instanceOf(AdvancedHttpRequest.class));
    assertTrue(request.isIgnoreProxySettings());
    assertThat(request.header("a", "b"), instanceOf(AdvancedHttpRequest.class));
    assertThat(request.headers("a", i1), instanceOf(AdvancedHttpRequest.class));
    assertThat(request.queryString("a", "b"), instanceOf(AdvancedHttpRequest.class));
    assertThat(request.queryStrings("a", i1), instanceOf(AdvancedHttpRequest.class));
  }

}
