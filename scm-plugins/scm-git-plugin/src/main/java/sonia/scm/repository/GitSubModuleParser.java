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

package sonia.scm.repository;

import sonia.scm.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public final class GitSubModuleParser {

  private GitSubModuleParser() {
  }

  public static Map<String, SubRepository> parse(String content) {
    Map<String, SubRepository> subRepositories = new HashMap<>();
    try (Scanner scanner = new Scanner(content)) {
      SubRepository repository = null;
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (Util.isNotEmpty(line)) {
          line = line.trim();
          if (line.startsWith("[") && line.endsWith("]")) {
            repository = new SubRepository();
          } else if (line.startsWith("path")) {
            subRepositories.put(getValue(line), repository);
          } else if (line.startsWith("url")) {
            repository.setRepositoryUrl(getValue(line));
          }
        }
      }
    }
    return subRepositories;
  }

  private static String getValue(String line) {
    return line.split("=")[1].trim();
  }
}
