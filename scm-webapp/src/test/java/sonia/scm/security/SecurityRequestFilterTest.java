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
    
package sonia.scm.security;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.util.ThreadContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SubjectAware(configuration = "classpath:sonia/scm/shiro-001.ini")
public class SecurityRequestFilterTest {

  @Rule
  public ShiroRule shiroRule = new ShiroRule();

  @Mock
  private ResourceInfo resourceInfo;
  @Mock
  private ContainerRequestContext context;
  @InjectMocks
  private SecurityRequestFilter securityRequestFilter;

  {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldAllowUnauthenticatedAccessForAnnotatedMethod() throws NoSuchMethodException {
    when(resourceInfo.getResourceMethod()).thenReturn(SecurityTestClass.class.getMethod("anonymousAccessAllowed"));

    securityRequestFilter.filter(context);
  }

  @Test(expected = AuthenticationException.class)
  public void shouldRejectUnauthenticatedAccessForAnnotatedMethod() throws NoSuchMethodException {
    when(resourceInfo.getResourceMethod()).thenReturn(SecurityTestClass.class.getMethod("loginRequired"));

    securityRequestFilter.filter(context);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldAllowAuthenticatedAccessForMethodWithoutAnnotation() throws NoSuchMethodException {
    when(resourceInfo.getResourceMethod()).thenReturn(SecurityTestClass.class.getMethod("loginRequired"));

    securityRequestFilter.filter(context);
  }

  private static class SecurityTestClass {
    @AllowAnonymousAccess
    public void anonymousAccessAllowed() {
    }

    public void loginRequired() {
    }
  }
}
