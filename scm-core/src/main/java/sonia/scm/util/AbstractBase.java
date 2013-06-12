/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
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
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

//~--- JDK imports ------------------------------------------------------------

import java.math.BigInteger;

/**
 * Abstract base class for encoding and decoding BaseX.
 *
 * @author Sebastian Sdorra
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
