package sonia.scm.security;

import org.apache.shiro.SecurityUtils;
import sonia.scm.SCMContext;

public class Authentications {

  public static boolean isAuthenticatedSubjectAnonymous() {
    return SecurityUtils.getSubject().getPrincipal().equals(SCMContext.USER_ANONYMOUS);
  }

  public static boolean isSubjectAnonymous(String principal) {
    return principal.equals(SCMContext.USER_ANONYMOUS);
  }
}
