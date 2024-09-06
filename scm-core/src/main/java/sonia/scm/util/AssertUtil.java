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


import sonia.scm.Validateable;

import java.util.Collection;


public final class AssertUtil
{

  private AssertUtil() {}



  public static void assertIsNotEmpty(String value)
  {
    if (Util.isEmpty(value))
    {
      throw new IllegalStateException("value is empty");
    }
  }

  public static void assertIsNotEmpty(Object[] array)
  {
    if (Util.isEmpty(array))
    {
      throw new IllegalStateException("array is empty");
    }
  }

  public static void assertIsNotEmpty(Collection<?> collection)
  {
    if (Util.isEmpty(collection))
    {
      throw new IllegalStateException("collection is empty");
    }
  }

  public static void assertIsNotNull(Object object)
  {
    if (object == null)
    {
      throw new IllegalStateException("object is required");
    }
  }

  public static void assertIsValid(Validateable validateable)
  {
    assertIsNotNull(validateable);

    if (!validateable.isValid())
    {
      throw new IllegalStateException("object is not valid");
    }
  }

  /**
   * @since 1.4
   */
  public static void assertPositive(int value)
  {
    if (value < 0)
    {
      throw new IllegalArgumentException("value is smaller then 0");
    }
  }
}
