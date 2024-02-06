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


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.math.BigInteger;

/**
 * Abstract base class for encoding and decoding BaseX.
 *
 * @since 1.21
 */
public abstract class AbstractBase
{

  /**
   * Decode a BaseX string to a BigInteger value.
   *
   *
   * @param chars char table
   * @param base base value
   * @param value BaseX string to decode
   *
   * @return decoded value
   */
  protected static BigInteger decode(String chars, BigInteger base,
    String value)
  {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(value),
      "string must not be empty");

    BigInteger result = BigInteger.ZERO;
    int digits = value.length();

    for (int index = 0; index < digits; index++)
    {
      int digit = chars.indexOf(value.charAt(digits - index - 1));

      result = result.add(BigInteger.valueOf(digit).multiply(base.pow(index)));
    }

    return result;
  }

  /**
   * Encode BigInteger value to a BaseX string.
   *
   * 
   * @param chars char table
   * @param base base value
   * @param value BigInteger value to encode
   *
   * @return encoded BaseX string
   */
  protected static String encode(String chars, BigInteger base,
    BigInteger value)
  {
    Preconditions.checkArgument(value.compareTo(BigInteger.ZERO) >= 0,
      "value must be positive");

    StringBuilder buffer = new StringBuilder();

    while (value.compareTo(BigInteger.ZERO) == 1)
    {
      BigInteger[] divmod = value.divideAndRemainder(base);

      value = divmod[0];

      int digit = divmod[1].intValue();

      buffer.insert(0, chars.charAt(digit));
    }

    String result = buffer.toString();

    if (result.length() == 0)
    {
      result = chars.substring(0, 1);
    }

    return result;
  }
}
