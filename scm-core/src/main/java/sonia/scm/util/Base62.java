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
    
package sonia.scm.util;

import java.math.BigInteger;

/**
 * Provides Base62 encoding and decoding for long values.
 *
 * @since 1.21
 */
public final class Base62 extends AbstractBase
{

  /** base value */
  private static final BigInteger BASE = BigInteger.valueOf(62L);

  /** char table */
  private static final String CHARS =
    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";


  /**
   * Decode a Base62 string to a long value.
   *
   *
   * @param value Base62 string value
   *
   * @return decoded long value
   */
  public static long decode(String value)
  {
    return decode(CHARS, BASE, value).longValue();
  }

  /**
   * Encode long value to a Base62 string.
   *
   *
   * @param value long value to encode
   *
   * @return encoded Base62 string
   */
  public static String encode(long value)
  {
    return encode(CHARS, BASE, BigInteger.valueOf(value));
  }
}
