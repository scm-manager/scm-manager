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
