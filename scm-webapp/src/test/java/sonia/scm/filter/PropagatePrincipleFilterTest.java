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
    
package sonia.scm.filter;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AnonymousMode;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link PropagatePrincipleFilter}.
 * 
 */
@RunWith(MockitoJUnitRunner.class)
@SubjectAware(configuration = "classpath:sonia/scm/shiro-001.ini")
public class PropagatePrincipleFilterTest {

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
  
  private PropagatePrincipleFilter propagatePrincipleFilter;
  
  @Rule
  public ShiroRule shiro = new ShiroRule();
  
  /**
   * Prepare object under test and mocks.
   */
  @Before
  public void setUp(){
    this.configuration = new ScmConfiguration();
    this.propagatePrincipleFilter = new PropagatePrincipleFilter(configuration);
  }
  
  /**
   * Tests filter without prior authentication.
   * 
   * @throws IOException
   * @throws ServletException 
   */
  @Test
  public void testAnonymous() throws IOException, ServletException {
    propagatePrincipleFilter.doFilter(request, response, chain);
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
    configuration.setAnonymousMode(AnonymousMode.FULL);
    
    // execute
    propagatePrincipleFilter.doFilter(request, response, chain);
    
    // verify and capture
    verify(request).setAttribute(PropagatePrincipleFilter.ATTRIBUTE_REMOTE_USER, SCMContext.USER_ANONYMOUS);
    verify(chain).doFilter(requestCaptor.capture(), responseCaptor.capture());
    
    // assert
    HttpServletRequest captured = requestCaptor.getValue();
    assertEquals(SCMContext.USER_ANONYMOUS, captured.getRemoteUser());
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

    // execute
    propagatePrincipleFilter.doFilter(request, response, chain);
    
    // verify and capture
    verify(request).setAttribute(PropagatePrincipleFilter.ATTRIBUTE_REMOTE_USER, "trillian");
    verify(chain).doFilter(requestCaptor.capture(), responseCaptor.capture());
    
    // assert
    HttpServletRequest captured = requestCaptor.getValue();
    assertEquals("trillian", captured.getRemoteUser());
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
}
