/**
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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Validateable;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public final class AssertUtil
{

  /**
   * Constructs ...
   *
   */
  private AssertUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param value
   */
  public static void assertIsNotEmpty(String value)
  {
    if (Util.isEmpty(value))
    {
      throw new IllegalStateException("value is empty");
    }
  }

  /**
   * Method description
   *
   *
   * @param array
   */
  public static void assertIsNotEmpty(Object[] array)
  {
    if (Util.isEmpty(array))
    {
      throw new IllegalStateException("array is empty");
    }
  }

  /**
   * Method description
   *
   *
   * @param collection
   */
  public static void assertIsNotEmpty(Collection<?> collection)
  {
    if (Util.isEmpty(collection))
    {
      throw new IllegalStateException("collection is empty");
    }
  }

  /**
   * Method description
   *
   *
   * @param object
   */
  public static void assertIsNotNull(Object object)
  {
    if (object == null)
    {
      throw new IllegalStateException("object is required");
    }
  }

  /**
   * Method description
   *
   *
   * @param validateable
   */
  public static void assertIsValid(Validateable validateable)
  {
    assertIsNotNull(validateable);

    if (!validateable.isValid())
    {
      throw new IllegalStateException("object is not valid");
    }
  }

  /**
   * throws an IllegalArgumentException if the value is smaller then 0
   *
   *
   * @param value
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
