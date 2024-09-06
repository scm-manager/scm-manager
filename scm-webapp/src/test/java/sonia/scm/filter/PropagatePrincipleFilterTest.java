/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
