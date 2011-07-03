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



package sonia.scm.cli;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.math.BigInteger;

/**
 *
 * @author Sebastian Sdorra
 */
public class ConvertUtil
{

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(ConvertUtil.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   * @param value
   *
   * @return
   */
  public static Object convertString(Class<?> type, String value)
  {
    Object result = null;

    try
    {
      if (type.isAssignableFrom(String.class))
      {
        result = value;
      }
      else if (type.isAssignableFrom(Short.class))
      {
        result = Short.parseShort(value);
      }
      else if (type.isAssignableFrom(Integer.class))
      {
        result = Integer.parseInt(value);
      }
      else if (type.isAssignableFrom(Long.class))
      {
        result = Long.parseLong(value);
      }
      else if (type.isAssignableFrom(BigInteger.class))
      {
        result = new BigInteger(value);
      }
      else if (type.isAssignableFrom(Float.class))
      {
        result = Float.parseFloat(value);
      }
      else if (type.isAssignableFrom(Double.class))
      {
        result = Double.parseDouble(value);
      }
      else if (type.isAssignableFrom(Boolean.class))
      {
        result = Boolean.parseBoolean(value);
      }
    }
    catch (NumberFormatException ex)
    {
      logger.debug(ex.getMessage(), ex);
    }

    return result;
  }
}
