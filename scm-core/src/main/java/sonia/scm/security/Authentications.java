package sonia.scm.security;

import org.apache.shiro.SecurityUtils;
import sonia.scm.SCMContext;

public class Authentications {

  public static boolean isAuthenticatedSubjectAnonymous() {
    return isSubjectAnonymous((String) SecurityUtils.getSubject().getPrincipal());
  }

  public static boolean isSubjectAnonymous(String principal) {
    return SCMContext.USER_ANONYMOUS.equals(principal);
  }
}
