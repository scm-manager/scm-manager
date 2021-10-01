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
    return isAuthenticationRequest(uri) && !isLogoutMethod(request);
  }

  private static boolean isLogoutMethod(HttpServletRequest request) {
    return "DELETE".equals(request.getMethod());
  }

  public static boolean isAuthenticationRequest(String uri) {
    return URI_LOGIN_PATTERN.matcher(uri).matches();
  }

  public static boolean isIndexRequest(HttpServletRequest request) {
    String uri = request.getRequestURI().substring(request.getContextPath().length());
    return isIndexRequest(uri);
  }

  public static boolean isIndexRequest(String uri) {
    return URI_INDEX_PATTERN.matcher(uri).matches();
  }

}
