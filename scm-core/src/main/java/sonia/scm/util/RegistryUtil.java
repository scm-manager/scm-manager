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

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.io.SimpleCommand;
import sonia.scm.io.SimpleCommandResult;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Scanner;

/**
 * @author Sebastian Sdorra
 */
public final class RegistryUtil {

  /**
   * the logger for RegistryUtil
   */
  private static final Logger logger =
    LoggerFactory.getLogger(RegistryUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   */
  private RegistryUtil() {
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @param key
   * @return
   */
  public static String getRegistryValue(String key) {
    return getRegistryValue(key, null, null);
  }

  /**
   * Method description
   *
   * @param key
   * @param defaultValue
   * @return
   */
  public static String getRegistryValue(String key, String defaultValue) {
    return getRegistryValue(key, null, defaultValue);
  }

  /**
   * Method description
   *
   * @param key
   * @param subKey
   * @param defaultValue
   * @return
   */
  public static String getRegistryValue(String key, String subKey,
                                        String defaultValue) {
    String value = defaultValue;
    SimpleCommand command = null;

    if (subKey != null) {
      command = new SimpleCommand("reg", "query", key, "/v", subKey);
    } else {
      command = new SimpleCommand("reg", "query", key, "/ve");
    }

    try {
      SimpleCommandResult result = command.execute();

      if (result.isSuccessfull()) {
        String output = result.getOutput();
        try (Scanner scanner = new Scanner(output)) {

          while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            int index = line.indexOf("REG_SZ");

            if (index > 0) {
              value = line.substring(index + "REG_SZ".length()).trim();

              if (value.startsWith("\"")) {
                value = value.substring(1);
                value = value.substring(0, value.indexOf('"'));
              }

              if (logger.isDebugEnabled()) {
                logger.debug("registry value {} at {}", value, key);
              }

              break;
            }
          }
        }
      }
    } catch (IOException ex) {
      logger.error(ex.getMessage(), ex);
    }

    return value;
  }
}
