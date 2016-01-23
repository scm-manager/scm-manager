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

package sonia.scm.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.util.HttpUtil;

/**
 * Unit tests for {@link XsrfProtectionFilter}.
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class XsrfProtectionFilterTest {

  @Mock
  private HttpServletRequest request;
  
  @Mock
  private HttpServletResponse response;
  
  @Mock
  private HttpSession session;
  
  @Mock
  private FilterChain chain;
  
  private final XsrfProtectionFilter filter = new XsrfProtectionFilter();
  
  /**
   * Prepare mocks for testing.
   */
  @Before
  public void setUp(){
    when(request.getSession(true)).thenReturn(session);
    when(request.getContextPath()).thenReturn("/scm");
  }

  /**
   * Test filter method for non web interface clients.
   * 
   * @throws IOException
   * @throws ServletException 
   */
  @Test
  public void testDoFilterFromNonWuiClient() throws IOException, ServletException
  {
    filter.doFilter(request, response, chain);
    verify(chain).doFilter(request, response);
  }
  
  /**
   * Test filter method for first web interface request.
   * 
   * @throws IOException
   * @throws ServletException 
   */
  @Test
  public void testDoFilterIssuesTokenOnFirstWuiRequest() throws IOException, ServletException
  {
    when(request.getHeader(HttpUtil.HEADER_SCM_CLIENT)).thenReturn(HttpUtil.SCM_CLIENT_WUI);
    
    // call the filter
    filter.doFilter(request, response, chain);
    
    // capture cookie
    ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
    verify(response).addCookie(captor.capture());
    
    // check for cookie
    Cookie cookie = captor.getValue();
    assertEquals(XsrfProtectionFilter.KEY, cookie.getName());
    assertEquals("/scm", cookie.getPath());
    assertNotNull(cookie.getValue());
    
    // ensure filter chain is called
    verify(chain).doFilter(request, response);
  }
  
  /**
   * Test filter method on protected session with an invalid xsrf token.
   * 
   * @throws IOException
   * @throws ServletException 
   */
  @Test
  public void testDoFilterWithInvalidToken() throws IOException, ServletException {
    when(request.getHeader(HttpUtil.HEADER_SCM_CLIENT)).thenReturn(HttpUtil.SCM_CLIENT_WUI);
    when(request.getHeader(XsrfProtectionFilter.KEY)).thenReturn("invalidtoken");
    when(session.getAttribute(XsrfProtectionFilter.KEY)).thenReturn("mytoken");
    
    // call the filter
    filter.doFilter(request, response, chain);
    
    // ensure response send forbidden and the chain was never called
    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    verify(chain, never()).doFilter(request, response);
  }

  /**
   * Test filter method on protected session without xsrf token.
   * 
   * @throws IOException
   * @throws ServletException 
   */
  @Test
  public void testDoFilterOnProtectedSessionWithoutToken() throws IOException, ServletException {
    when(request.getHeader(HttpUtil.HEADER_SCM_CLIENT)).thenReturn(HttpUtil.SCM_CLIENT_WUI);
    when(session.getAttribute(XsrfProtectionFilter.KEY)).thenReturn("mytoken");
    
    // call the filter
    filter.doFilter(request, response, chain);
    
    // ensure response send forbidden and the chain was never called
    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    verify(chain, never()).doFilter(request, response);
  }

  /**
   * Test filter method on protected session with valid xsrf token.
   * 
   * @throws IOException
   * @throws ServletException 
   */  
  @Test
  public void testDoFilterOnProtectedSessionWithValidToken() throws IOException, ServletException {
    when(request.getHeader(HttpUtil.HEADER_SCM_CLIENT)).thenReturn(HttpUtil.SCM_CLIENT_WUI);
    when(request.getHeader(XsrfProtectionFilter.KEY)).thenReturn("mytoken");
    when(session.getAttribute(XsrfProtectionFilter.KEY)).thenReturn("mytoken");
    
    // call the filter
    filter.doFilter(request, response, chain);
    
    // ensure chain was called
    verify(chain).doFilter(request, response);
  }

}