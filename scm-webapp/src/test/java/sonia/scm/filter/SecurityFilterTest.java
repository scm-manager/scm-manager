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

package sonia.scm.filter;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

/**
 * Unit tests for {@link SecurityFilter}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
@SubjectAware(configuration = "classpath:sonia/scm/shiro-001.ini")
public class SecurityFilterTest {

  @Mock
  private HttpServletRequest request;
  
  @Captor
  private ArgumentCaptor<HttpServletRequest> requestCaptor;
  
  @Mock
  private HttpServletResponse response;
  
  @Captor
  private ArgumentCaptor<HttpServletResponse> responseCaptor;
  
  @Mock
  private FilterChain chain;

  private ScmConfiguration configuration;
  
  private SecurityFilter securityFilter;
  
  @Rule
  public ShiroRule shiro = new ShiroRule();
  
  /**
   * Prepare object under test and mocks.
   */
  @Before
  public void setUp(){
    this.configuration = new ScmConfiguration();
    this.securityFilter = new SecurityFilter(configuration);
    
    when(request.getContextPath()).thenReturn("/scm");
  }
  
  /**
   * Tests filter on authentication endpoint.
   * 
   * @throws IOException
   * @throws ServletException 
   */
  @Test
  public void testDoOnAuthenticationUrl() throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("/scm/api/rest/authentication");
    securityFilter.doFilter(request, response, chain);
    verify(request, never()).setAttribute(Mockito.anyString(), Mockito.any());
    verify(chain).doFilter(request, response);
  }
  
  /**
   * Tests filter without prior authentication.
   * 
   * @throws IOException
   * @throws ServletException 
   */
  @Test
  public void testAnonymous() throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("/scm/api");
    securityFilter.doFilter(request, response, chain);
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
  }
  
  /**
   * Tests filter without prior authentication and enabled anonymous access.
   * 
   * @throws IOException
   * @throws ServletException 
   */
  @Test
  public void testAnonymousWithAccessEnabled() throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("/scm/api");
    configuration.setAnonymousAccessEnabled(true);
    
    // execute
    securityFilter.doFilter(request, response, chain);
    
    // verify and capture
    verify(request).setAttribute(SecurityFilter.ATTRIBUTE_REMOTE_USER, SCMContext.USER_ANONYMOUS);
    verify(chain).doFilter(requestCaptor.capture(), responseCaptor.capture());
    
    // assert
    HttpServletRequest captured = requestCaptor.getValue();
    assertEquals(SCMContext.USER_ANONYMOUS, captured.getRemoteUser());
    assertEquals(SCMContext.ANONYMOUS, captured.getUserPrincipal());
  }
  
  /**
   * Tests filter with prior authentication.
   * 
   * @throws IOException
   * @throws ServletException
   */
  @Test
  public void testAuthenticated() throws IOException, ServletException {
    authenticateUser(UserTestData.createTrillian());
    when(request.getRequestURI()).thenReturn("/scm/api");
    
    // execute
    securityFilter.doFilter(request, response, chain);
    
    // verify and capture
    verify(request).setAttribute(SecurityFilter.ATTRIBUTE_REMOTE_USER, "trillian");
    verify(chain).doFilter(requestCaptor.capture(), responseCaptor.capture());
    
    // assert
    HttpServletRequest captured = requestCaptor.getValue();
    assertEquals(HttpServletRequest.BASIC_AUTH, captured.getAuthType());
    assertEquals("trillian", captured.getRemoteUser());
    assertThat(captured.getUserPrincipal(), instanceOf(User.class));
    assertEquals("trillian", captured.getUserPrincipal().getName());
  }
  
  /**
   * Tests filter without permissions.
   * 
   * @throws IOException
   * @throws ServletException
   */
  @Test
  public void testForbidden() throws IOException, ServletException {
    authenticateUser(UserTestData.createTrillian());
    when(request.getRequestURI()).thenReturn("/scm/api");
    
    // execute
    securityFilter = new AccessForbiddenSecurityFilter(configuration);
    securityFilter.doFilter(request, response, chain);
    
    // assert
    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
  }
  
  /**
   * Tests filter unauthenticated and without permissions.
   * 
   * @throws IOException
   * @throws ServletException
   */
  @Test
  public void testUnauthorized() throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("/scm/api");
    
    // execute
    securityFilter = new AccessForbiddenSecurityFilter(configuration);
    securityFilter.doFilter(request, response, chain);
    
    // assert
    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }

  private void authenticateUser(User user) {
    SimplePrincipalCollection spc = new SimplePrincipalCollection();
    spc.add(user.getName(), "unit-test");
    spc.add(user, "unit-test");
    
    Subject subject = new Subject.Builder()
      .authenticated(true)
      .principals(spc)
      .buildSubject();
    
    shiro.setSubject(subject);
  }
  
  private static class AccessForbiddenSecurityFilter extends SecurityFilter {
    
    private AccessForbiddenSecurityFilter(ScmConfiguration configuration) {
      super(configuration);
    }

    @Override
    protected boolean hasPermission(Subject subject) {
      return false;
    }
    
  }

}