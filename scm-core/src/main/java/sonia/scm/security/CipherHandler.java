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

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Encrypts and decrypts string values.
 *
 * @since 1.7
 */
public interface CipherHandler
{

  /**
   * Decrypts the given value.
   */
  String decode(String value);

  /**
   * Decrypts the given value. If not implemented explicitly, this creates a string
   * from the byte array, decodes this with {@link #decode(String)}, and interprets
   * this string as base 64 encoded byte array.
   * <p>
   * if {@link #encode(byte[])} is overridden by an implementation, this has to be
   * implemented accordingly.
   *
   * @param value encrypted value
   *
   * @return decrypted value
   */
  default byte[] decode(byte[] value) {
    return Base64.getDecoder().decode(decode(new String(value, UTF_8)));
  }

  /**
   * Encrypts the given value.
   */
  String encode(String value);

  /**
   * Encrypts the given value. If not implemented explicitly, this encoded the given
   * byte array as a base 64 string, encodes this string with {@link #encode(String)},
   * and returns the bytes of this resulting string.
   * <p>
   * if {@link #decode(byte[])} is overridden by an implementation, this has to be
   * implemented accordingly.
   *
   * @param value plain byte array to encrypt.
   *
   * @return encrypted value
   */
  default byte[] encode(byte[] value) {
    return encode(Base64.getEncoder().encodeToString(value)).getBytes(UTF_8);
  }
}
