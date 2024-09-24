/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
