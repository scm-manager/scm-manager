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
    
package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.plugin.Extension;
import sonia.scm.security.BearerToken;
import sonia.scm.security.SessionId;
import sonia.scm.util.HttpUtil;

/**
 * Creates an {@link BearerToken} from the {@link HttpUtil#COOKIE_BEARER_AUTHENTICATION}
 * cookie.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Extension
public class CookieBearerWebTokenGenerator implements WebTokenGenerator
{

  /**
   * Creates an {@link BearerToken} from the {@link HttpUtil#COOKIE_BEARER_AUTHENTICATION}
   * cookie.
   *
   * @param request http servlet request
   *
   * @return {@link BearerToken} or {@code null}
   */
  @Override
  public BearerToken createToken(HttpServletRequest request) {
    BearerToken token = null;
    Cookie[] cookies = request.getCookies();

    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (HttpUtil.COOKIE_BEARER_AUTHENTICATION.equals(cookie.getName())) {
          token = BearerToken.create(SessionId.from(request).orElse(null), cookie.getValue());

          break;
        }
      }
    }

    return token;
  }
}
