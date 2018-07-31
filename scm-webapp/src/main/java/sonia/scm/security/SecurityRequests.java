package sonia.scm.security;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * Created by masuewer on 04.07.18.
 */
public final class SecurityRequests {

  private static final Pattern URI_LOGIN_PATTERN = Pattern.compile("/api/rest(?:/v2)?/auth/access_token");

  private SecurityRequests() {}

  public static boolean isAuthenticationRequest(HttpServletRequest request) {
    String uri = request.getRequestURI().substring(request.getContextPath().length());
    return isAuthenticationRequest(uri);
  }

  public static boolean isAuthenticationRequest(String uri) {
    return URI_LOGIN_PATTERN.matcher(uri).matches();
  }

}
