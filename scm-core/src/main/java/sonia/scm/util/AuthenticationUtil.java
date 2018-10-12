package sonia.scm.util;

import org.apache.shiro.SecurityUtils;

public class AuthenticationUtil {

  public static String getAuthenticatedUsername() {
    Object subject = SecurityUtils.getSubject().getPrincipal();
    AssertUtil.assertIsNotNull(subject);
    return subject.toString();
  }
}
