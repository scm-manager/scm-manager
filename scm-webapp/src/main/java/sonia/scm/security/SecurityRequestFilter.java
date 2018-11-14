package sonia.scm.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
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
