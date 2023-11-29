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

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@Provider
public class SecurityRequestFilter  implements ContainerRequestFilter {

  private static final Logger LOG = LoggerFactory.getLogger(SecurityRequestFilter.class);

  @Context
  private ResourceInfo resourceInfo;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    Method resourceMethod = resourceInfo.getResourceMethod();
    if (hasPermission() || anonymousAccessIsAllowed(resourceMethod)) {
      LOG.debug("allowed unauthenticated request to method {}", resourceMethod);
      // nothing further to do
    } else {
      LOG.debug("blocked unauthenticated request to method {}", resourceMethod);
      throw new AuthenticationException();
    }
  }

  private boolean anonymousAccessIsAllowed(Method method) {
    return method.isAnnotationPresent(AllowAnonymousAccess.class)
      || method.getDeclaringClass().isAnnotationPresent(AllowAnonymousAccess.class);
  }

  private boolean hasPermission() {
    Subject subject = SecurityUtils.getSubject();
    return subject.isAuthenticated() || subject.isRemembered();
  }
}
