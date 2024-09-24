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
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.web.VndMediaType;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static sonia.scm.security.TokenExpiredFilter.TOKEN_EXPIRED_ERROR_CODE;

@RunWith(MockitoJUnitRunner.class)
public class TokenExpiredFilterTest {

  @Mock
  private FilterChain chain;
  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private AccessTokenCookieIssuer accessTokenCookieIssuer;

  @Test
  public void shouldReturnSpecificErrorResponseAndInvalidateCookie() throws IOException, ServletException {
    final TokenExpiredFilter filter = new TokenExpiredFilter(accessTokenCookieIssuer, objectMapper);
    doThrow(TokenExpiredException.class).when(chain).doFilter(request, response);

    filter.doFilter(request, response, chain);

    verify(chain, atLeastOnce()).doFilter(request, response);
    verify(accessTokenCookieIssuer, atLeastOnce()).invalidate(request, response);
    verify(response, atLeastOnce()).setContentType(VndMediaType.ERROR_TYPE);
    verify(objectMapper).writeValue((ServletOutputStream) any(), argThat((ErrorDto errorDto) -> {
      assertEquals(TOKEN_EXPIRED_ERROR_CODE, errorDto.getErrorCode());
      return true;
    }));
  }

}
