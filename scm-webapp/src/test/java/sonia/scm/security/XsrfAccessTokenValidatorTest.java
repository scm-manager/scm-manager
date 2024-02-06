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


import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests {@link XsrfAccessTokenValidator}.
 *
 */
@ExtendWith(MockitoExtension.class)
class XsrfAccessTokenValidatorTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private AccessToken accessToken;

  private final XsrfExcludes excludes = new XsrfExcludes();

  private XsrfAccessTokenValidator validator;

  /**
   * Prepare object under test.
   */
  @BeforeEach
  void prepareObjectUnderTest() {
    validator = new XsrfAccessTokenValidator(() -> request, excludes);
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
      assertThat(validator.validate(accessToken)).isTrue();
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
      assertThat(validator.validate(accessToken)).isFalse();
    }

    /**
     * Tests {@link XsrfAccessTokenValidator#validate(AccessToken)} without header.
     */
    @Test
    void testValidateWithoutHeader() {
      // prepare
      when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(Optional.of("abc"));

      // execute and assert
      assertThat(validator.validate(accessToken)).isFalse();
    }

    /**
     * Tests {@link XsrfAccessTokenValidator#validate(AccessToken)} without claims key.
     */
    @Test
    void testValidateWithoutClaimsKey() {
      // prepare
      when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(Optional.empty());

      // execute and assert
      assertThat(validator.validate(accessToken)).isTrue();
    }

    @Test
    void shouldNotValidateExcludedRequest() {
      excludes.add("/excluded");

      // prepare
      when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(Optional.of("abc"));
      when(request.getRequestURI()).thenReturn("/excluded");

      // execute and assert
      assertThat(validator.validate(accessToken)).isTrue();
    }


  }

  @ParameterizedTest
  @ValueSource(strings = {"GET", "HEAD", "OPTIONS"})
  void shouldNotValidateReadRequests(String method) {
    // prepare
    when(request.getMethod()).thenReturn(method);
    when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(Optional.of("abc"));

    // execute and assert
    assertThat(validator.validate(accessToken)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"GET", "HEAD", "OPTIONS"})
  void shouldFailValidationOfWriteRequests(String method) {
    // prepare
    when(request.getMethod()).thenReturn(method);
    when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(Optional.of("abc"));

    // execute and assert
    assertThat(validator.validate(accessToken)).isTrue();
  }
}
