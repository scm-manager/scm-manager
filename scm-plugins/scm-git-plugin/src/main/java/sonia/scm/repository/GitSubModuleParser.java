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
