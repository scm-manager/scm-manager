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
