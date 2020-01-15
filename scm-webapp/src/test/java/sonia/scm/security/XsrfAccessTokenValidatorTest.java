/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */

package sonia.scm.security;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests {@link XsrfAccessTokenValidator}.
 *
 * @author Sebastian Sdorra
 */
@ExtendWith(MockitoExtension.class)
class XsrfAccessTokenValidatorTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private AccessToken accessToken;

  private XsrfAccessTokenValidator validator;

  /**
   * Prepare object under test.
   */
  @BeforeEach
  void prepareObjectUnderTest() {
    validator = new XsrfAccessTokenValidator(() -> request);
  }

  @Nested
  class RequestMethodPost {

    @BeforeEach
    void setRequestMethod() {
      lenient().when(request.getMethod()).thenReturn("POST");
    }

    /**
     * Tests {@link XsrfAccessTokenValidator#validate(AccessToken)}.
     */
    @Test
    void testValidate() {
      // prepare
      when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(Optional.of("abc"));
      when(request.getHeader(Xsrf.HEADER_KEY)).thenReturn("abc");

      // execute and assert
      assertTrue(validator.validate(accessToken));
    }

    /**
     * Tests {@link XsrfAccessTokenValidator#validate(AccessToken)} with wrong header.
     */
    @Test
    void testValidateWithWrongHeader() {
      // prepare
      when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(Optional.of("abc"));
      when(request.getHeader(Xsrf.HEADER_KEY)).thenReturn("123");

      // execute and assert
      assertFalse(validator.validate(accessToken));
    }

    /**
     * Tests {@link XsrfAccessTokenValidator#validate(AccessToken)} without header.
     */
    @Test
    void testValidateWithoutHeader() {
      // prepare
      when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(Optional.of("abc"));

      // execute and assert
      assertFalse(validator.validate(accessToken));
    }

    /**
     * Tests {@link XsrfAccessTokenValidator#validate(AccessToken)} without claims key.
     */
    @Test
    void testValidateWithoutClaimsKey() {
      // prepare
      when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(Optional.empty());

      // execute and assert
      assertTrue(validator.validate(accessToken));
    }

  }

  @ParameterizedTest
  @CsvSource({"GET", "HEAD", "OPTIONS"})
  void shouldNotValidateReadRequests(String method) {
    // prepare
    when(request.getMethod()).thenReturn(method);
    when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(Optional.of("abc"));

    // execute and assert
    assertTrue(validator.validate(accessToken));
  }

  @ParameterizedTest
  @CsvSource({"POST", "PUT", "DELETE", "PATCH"})
  void shouldFailValidationOfWriteRequests(String method) {
    // prepare
    when(request.getMethod()).thenReturn(method);
    when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(Optional.of("abc"));

    // execute and assert
    assertFalse(validator.validate(accessToken));
  }
}
