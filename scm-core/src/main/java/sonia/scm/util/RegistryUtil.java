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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.io.SimpleCommand;
import sonia.scm.io.SimpleCommandResult;

import java.io.IOException;

import java.util.Scanner;

public final class RegistryUtil {

  private static final Logger logger =
    LoggerFactory.getLogger(RegistryUtil.class);


  private RegistryUtil() {
  }


  public static String getRegistryValue(String key) {
    return getRegistryValue(key, null, null);
  }

  public static String getRegistryValue(String key, String defaultValue) {
    return getRegistryValue(key, null, defaultValue);
  }

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
