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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the {@link XsrfCookies} util class.
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class XsrfCookiesTest {

  @Mock
  private HttpServletRequest request;
  
  @Mock
  private HttpServletResponse response;
  
  /**
   * Prepare mocks for testing.
   */
  @Before
  public void prepareMocks(){
    when(request.getContextPath()).thenReturn("/scm");
  }
  
  /**
   * Tests create method.
   */
  @Test
  public void testCreate()
  {
    XsrfCookies.create(request, response, "mytoken");
    
    // capture cookie
    ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
    verify(response).addCookie(captor.capture());
    
    // check for cookie
    Cookie cookie = captor.getValue();
    assertEquals(XsrfProtectionFilter.KEY, cookie.getName());
    assertEquals("/scm", cookie.getPath());
    assertEquals("mytoken", cookie.getValue());
  }
  
  /**
   * Tests remove method.
   */
  @Test
  public void testRemove(){
    Cookie cookie = new Cookie(XsrfProtectionFilter.KEY, "mytoken");
    cookie.setMaxAge(15);
    when(request.getCookies()).thenReturn(new Cookie[]{cookie});
    XsrfCookies.remove(request, response);
    
    // capture cookie
    ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
    verify(response).addCookie(captor.capture());
    
    // check the captured cookie
    Cookie c = captor.getValue();
    assertEquals("cookie max age should be set to 0", 0, c.getMaxAge());
    assertEquals("cookie path should be equals", cookie.getPath(), c.getPath());
    assertNull("cookie value shuld be null", c.getValue());
  }

}