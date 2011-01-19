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

import sonia.scm.io.SimpleCommand;
import sonia.scm.io.SimpleCommandResult;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Scanner;

/**
 *
 * @author Sebastian Sdorra
 */
public class RegistryUtil
{

  /** the logger for RegistryUtil */
  private static final Logger logger =
    LoggerFactory.getLogger(RegistryUtil.class);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  public static String getRegistryValue(String key)
  {
    return getRegistryValue(key, null, null);
  }

  /**
   * Method description
   *
   *
   * @param key
   * @param defaultValue
   *
   * @return
   */
  public static String getRegistryValue(String key, String defaultValue)
  {
    return getRegistryValue(key, null, defaultValue);
  }

  /**
   * Method description
   *
   *
   * @param key
   * @param subKey
   * @param defaultValue
   *
   * @return
   */
  public static String getRegistryValue(String key, String subKey,
          String defaultValue)
  {
    String value = defaultValue;
    SimpleCommand command = null;

    if (subKey != null)
    {
      command = new SimpleCommand("reg", "query", key, "/v", subKey);
    }
    else
    {
      command = new SimpleCommand("reg", "query", key);
    }

    try
    {
      SimpleCommandResult result = command.execute();

      if (result.isSuccessfull())
      {
        String output = result.getOutput();
        Scanner scanner = new Scanner(output);

        while (scanner.hasNextLine())
        {
          String line = scanner.nextLine();
          int index = line.indexOf("REG_SZ");

          if (index > 0)
          {
            value = line.substring(index + "REG_SZ".length()).trim();

            if (value.startsWith("\""))
            {
              value = value.substring(1);
              value = value.substring(0, value.indexOf("\""));
            }

            if (logger.isDebugEnabled())
            {
              logger.debug("registry value {} at {}", value, key);
            }

            break;
          }
        }
      }
    }
    catch (IOException ex)
    {
      logger.error(ex.getMessage(), ex);
    }

    return value;
  }
}
