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
