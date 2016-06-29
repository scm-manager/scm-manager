/***
 * Copyright (c) 2015, Sebastian Sdorra
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
 * https://bitbucket.org/sdorra/scm-manager
 * 
 */

package sonia.scm.filter;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import java.io.IOException;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;
import sonia.scm.AbstractTestBase;

/**
 * Unit tests for {@link MDCFilter}.
 * 
 * @author Sebastian Sdorra <sebastian.sdorra@gmail.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class MDCFilterTest extends AbstractTestBase {
  
  @Rule
  public ShiroRule shiro = new ShiroRule();
  
  @Mock
  private HttpServletRequest request;
  
  @Mock
  private HttpServletResponse response;
  
  private final MDCFilter filter = new MDCFilter();

  /**
   * Tests {@link MDCFilter#doFilter(HttpServletRequest, HttpServletResponse, FilterChain)}.
   * 
   * @throws IOException
   * @throws ServletException 
   */
  @Test
  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testDoFilter() throws IOException, ServletException
  {
    when(request.getRequestURI()).thenReturn("api/v1/repositories");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    when(request.getRemoteHost()).thenReturn("localhost");
    when(request.getMethod()).thenReturn("GET");
    
    MDCCapturingFilterChain chain = new MDCCapturingFilterChain();
    filter.doFilter(request, response, chain);
    
    assertNotNull(chain.ctx);
    assertEquals("trillian", chain.ctx.get(MDCFilter.MDC_USERNAME));
    assertEquals("api/v1/repositories", chain.ctx.get(MDCFilter.MDC_REQUEST_URI));
    assertEquals("127.0.0.1", chain.ctx.get(MDCFilter.MDC_CLIEN_IP));
    assertEquals("localhost", chain.ctx.get(MDCFilter.MDC_CLIEN_HOST));
    assertEquals("GET", chain.ctx.get(MDCFilter.MDC_REQUEST_METHOD));
  }
  
  /**
   * Tests {@link MDCFilter#doFilter(HttpServletRequest, HttpServletResponse, FilterChain)} as anonymous user.
   * 
   * @throws IOException
   * @throws ServletException 
   */
  @Test
  @SubjectAware
  public void testDoFilterAsAnonymous() throws IOException, ServletException {
    MDCCapturingFilterChain chain = new MDCCapturingFilterChain();
    filter.doFilter(request, response, chain);
    
    assertNotNull(chain.ctx);
    assertEquals("anonymous", chain.ctx.get(MDCFilter.MDC_USERNAME));
  }
  
  private static class MDCCapturingFilterChain implements FilterChain {

    private Map<String, String> ctx;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
      this.ctx = MDC.getCopyOfContextMap();
    }
    
  }

}