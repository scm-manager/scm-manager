package sonia.scm.security;

import java.security.SecureRandom;

public class SecureKeyTestUtil {
  public static SecureKey createSecureKey() {
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    return new SecureKey(bytes, System.currentTimeMillis());
  }
}
