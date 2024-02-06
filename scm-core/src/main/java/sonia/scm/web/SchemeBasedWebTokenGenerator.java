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


import com.google.common.base.Strings;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @since 2.0.0
 */
public abstract class SchemeBasedWebTokenGenerator implements WebTokenGenerator {

  /** authorization header */
  private static final String HEADER_AUTHORIZATION = "Authorization";

  private static final Logger LOG = LoggerFactory.getLogger(SchemeBasedWebTokenGenerator.class);

  protected abstract AuthenticationToken createToken(HttpServletRequest request, String scheme, String authorization);

  @Override
  public AuthenticationToken createToken(HttpServletRequest request) {
    AuthenticationToken token = null;
    String authorization = request.getHeader(HEADER_AUTHORIZATION);

    if (!Strings.isNullOrEmpty(authorization)) {
      String[] parts = authorization.split("\\s+");

      if (parts.length > 0) {
        token = createToken(request, parts[0], parts[1]);

        if (token == null) {
          LOG.debug("could not create token from authentication header");
        }
      } else {
        LOG.warn("found malformed authentication header");
      }
    }

    return token;
  }
}
