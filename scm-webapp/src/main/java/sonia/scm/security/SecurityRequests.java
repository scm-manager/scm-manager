package sonia.scm.security;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

import static sonia.scm.api.v2.resources.ScmPathInfo.REST_API_PATH;

/**
 * Created by masuewer on 04.07.18.
 */
public final class SecurityRequests {

  private static final Pattern URI_LOGIN_PATTERN = Pattern.compile(REST_API_PATH + "(?:/v2)?/auth/access_token");
  private static final Pattern URI_INDEX_PATTERN = Pattern.compile(REST_API_PATH + "/v2/?");

  private SecurityRequests() {}

  public static boolean isAuthenticationRequest(HttpServletRequest request) {
    String uri = request.getRequestURI().substring(request.getContextPath().length());
    return isAuthenticationRequest(uri);
  }

  public static boolean isAuthenticationRequest(String uri) {
    return URI_LOGIN_PATTERN.matcher(uri).matches();
  }

  public static boolean isIndexRequest(HttpServletRequest request) {
    String uri = request.getRequestURI().substring(request.getContextPath().length());
    return isAuthenticationRequest(uri);
  }

  public static boolean isIndexRequest(String uri) {
    return URI_INDEX_PATTERN.matcher(uri).matches();
  }

}
