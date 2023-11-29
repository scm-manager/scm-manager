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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import sonia.scm.Priority;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.web.VndMediaType;
import sonia.scm.web.filter.HttpFilter;

import java.io.IOException;

@WebElement("/*")
@Priority(Filters.PRIORITY_PRE_AUTHENTICATION)
@Singleton
public class TokenExpiredFilter extends HttpFilter {
  static final String TOKEN_EXPIRED_ERROR_CODE = "DDS8D8unr1";
  private static final Logger LOG = LoggerFactory.getLogger(TokenExpiredFilter.class);

  private final AccessTokenCookieIssuer accessTokenCookieIssuer;
  private final ObjectMapper objectMapper;

  @Inject
  public TokenExpiredFilter(AccessTokenCookieIssuer accessTokenCookieIssuer, ObjectMapper objectMapper) {
    this.accessTokenCookieIssuer = accessTokenCookieIssuer;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    try {
      chain.doFilter(request, response);
    } catch (TokenExpiredException ex) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Token expired", ex);
      } else {
        LOG.debug("Token expired");
      }
      handleTokenExpired(request, response);
    }
  }

  protected void handleTokenExpired(HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
    accessTokenCookieIssuer.invalidate(request, response);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(VndMediaType.ERROR_TYPE);
    final ErrorDto errorDto = new ErrorDto();
    errorDto.setMessage("Token Expired");
    errorDto.setErrorCode(TOKEN_EXPIRED_ERROR_CODE);
    errorDto.setTransactionId(MDC.get("transaction_id"));
    try (ServletOutputStream stream = response.getOutputStream()) {
      objectMapper.writeValue(stream, errorDto);
    }
  }
}
