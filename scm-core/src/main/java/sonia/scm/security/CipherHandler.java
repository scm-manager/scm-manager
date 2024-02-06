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
