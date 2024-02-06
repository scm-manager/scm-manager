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


import jakarta.servlet.http.HttpServletRequest;
import sonia.scm.plugin.Extension;
import sonia.scm.security.BearerToken;
import sonia.scm.security.SessionId;
import sonia.scm.util.HttpUtil;

/**
 * Creates a {@link BearerToken} from an authorization header with
 * bearer authorization.
 *
 * @since 2.0.0
 */
@Extension
public class BearerWebTokenGenerator extends SchemeBasedWebTokenGenerator
{

  /**
   * Creates a {@link BearerToken} from an authorization header
   * with bearer authorization.
   *
   * @param request http servlet request
   * @param scheme authorization scheme
   * @param authorization authorization payload
   *
   * @return {@link BearerToken} or {@code null}
   */
  @Override
  protected BearerToken createToken(HttpServletRequest request,
    String scheme, String authorization)
  {
    BearerToken token = null;

    if (HttpUtil.AUTHORIZATION_SCHEME_BEARER.equalsIgnoreCase(scheme)) {
      token = BearerToken.create(SessionId.from(request).orElse(null), authorization);
    }

    return token;
  }
}
