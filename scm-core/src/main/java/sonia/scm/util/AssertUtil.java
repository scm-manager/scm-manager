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

import sonia.scm.ArgumentIsInvalidException;
import sonia.scm.Validateable;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public class AssertUtil
{

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
