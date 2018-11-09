package sonia.scm.security;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;

public class SecurityInterceptor implements MethodInterceptor {

  @Override
  public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    if (hasPermission() || anonymousAccessIsAllowed(methodInvocation)) {
      return methodInvocation.proceed();
    } else {
      throw new AuthenticationException();
    }
  }

  private boolean anonymousAccessIsAllowed(MethodInvocation methodInvocation) {
    return methodInvocation.getMethod().isAnnotationPresent(AllowAnonymousAccess.class)
      || methodInvocation.getMethod().getDeclaringClass().isAnnotationPresent(AllowAnonymousAccess.class);
  }

  private boolean hasPermission() {
    Subject subject = SecurityUtils.getSubject();
    return subject.isAuthenticated() || subject.isRemembered();
  }
}
