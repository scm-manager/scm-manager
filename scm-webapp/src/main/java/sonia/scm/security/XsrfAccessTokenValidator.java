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

import com.google.common.collect.ImmutableSet;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Validates xsrf protected access tokens. The validator check if the current request contains an xsrf key which is
 * equal to the one in the access token. If the token does not contain a xsrf key, the check is passed by. The xsrf keys
 * are added by the {@link XsrfAccessTokenEnricher}.
 * 
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Extension
public class XsrfAccessTokenValidator implements AccessTokenValidator {

  private static final Set<String> ALLOWED_METHOD = ImmutableSet.of(
    "GET", "HEAD", "OPTIONS"
  );

  private final Provider<HttpServletRequest> requestProvider;
  private final XsrfExcludes excludes;
  
  /**
   * Constructs a new instance.
   *
   * @param requestProvider http request provider
   * @param excludes
   */
  @Inject
  public XsrfAccessTokenValidator(Provider<HttpServletRequest> requestProvider, XsrfExcludes excludes) {
    this.requestProvider = requestProvider;
    this.excludes = excludes;
  }
  
  @Override
  public boolean validate(AccessToken accessToken) {
    Optional<String> xsrfClaim = accessToken.getCustom(Xsrf.TOKEN_KEY);
    if (xsrfClaim.isPresent()) {
      HttpServletRequest request = requestProvider.get();

      if (excludes.contains(request.getRequestURI())) {
        return true;
      }

      String xsrfHeaderValue = request.getHeader(Xsrf.HEADER_KEY);
      return ALLOWED_METHOD.contains(request.getMethod().toUpperCase(Locale.ENGLISH))
        || xsrfClaim.get().equals(xsrfHeaderValue);
    }
    return true;
  }
  
}
