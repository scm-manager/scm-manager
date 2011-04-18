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

import sonia.scm.Validateable;

/**
 *
 * @author Sebastian Sdorra
 */
public class ValidationUtil
{

  /** Field description */
  private static final String REGEX_MAIL =
    "^[A-z0-9][\\w.-]*@[A-z0-9][\\w\\-\\.]+\\.[A-z0-9]{2,6}$";

  //~--- get methods ----------------------------------------------------------

  /**
   *  Method description
   *
   *
   *  @param value
   *
   *  @return
   */
  public static boolean isFilenameValid(String value)
  {
    AssertUtil.assertIsNotNull(value);

    return isNotContaining(value, "/", "\\", ":");
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public static boolean isMailAddressValid(String value)
  {
    AssertUtil.assertIsNotNull(value);

    return value.matches(REGEX_MAIL);
  }

  /**
   * Method description
   *
   *
   * @param value
   * @param notAllowedStrings
   *
   * @return
   */
  public static boolean isNotContaining(String value,
          String... notAllowedStrings)
  {
    AssertUtil.assertIsNotNull(value);

    boolean result = true;

    if (notAllowedStrings != null)
    {
      for (String nas : notAllowedStrings)
      {
        if (value.indexOf(nas) >= 0)
        {
          result = false;

          break;
        }
      }
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param validateable
   *
   * @return
   */
  public static boolean isValid(Validateable validateable)
  {
    return (validateable != null) && validateable.isValid();
  }
}
