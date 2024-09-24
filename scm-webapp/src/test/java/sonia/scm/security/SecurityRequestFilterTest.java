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
