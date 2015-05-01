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
import org.mockito.runners.MockitoJUnitRunner;

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