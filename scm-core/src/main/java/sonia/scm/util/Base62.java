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
