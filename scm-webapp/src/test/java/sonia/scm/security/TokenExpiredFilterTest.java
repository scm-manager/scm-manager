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
