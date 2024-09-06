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
