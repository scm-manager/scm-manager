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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

/**
 *
 * @author Sebastian Sdorra
 */
public class ServiceUtil
{

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(ServiceUtil.class);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   * @param def
   * @param <T>
   *
   * @return
   */
  public static <T> T getService(Class<T> type, T def)
  {
    T result = getService(type);

    if (result == null)
    {
      result = def;
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param <T>
   *
   * @return
   */
  public static <T> T getService(Class<T> type)
  {
    T result = null;

    try
    {
      ServiceLoader<T> loader = ServiceLoader.load(type);

      if (loader != null)
      {
        result = loader.iterator().next();
      }
    }
    catch (NoSuchElementException ex)
    {
      logger.debug(ex.getMessage(), ex);
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param <T>
   *
   * @return
   */
  public static <T> List<T> getServices(Class<T> type)
  {
    List<T> result = new ArrayList<T>();

    try
    {
      ServiceLoader<T> loader = ServiceLoader.load(type);

      if (loader != null)
      {
        for (T service : loader)
        {
          result.add(service);
        }
      }
    }
    catch (NoSuchElementException ex)
    {
      logger.debug(ex.getMessage(), ex);
    }

    return result;
  }
}
