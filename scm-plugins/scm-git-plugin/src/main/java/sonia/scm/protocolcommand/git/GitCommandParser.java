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
    
package sonia.scm.protocolcommand.git;

import java.util.ArrayList;
import java.util.List;

class GitCommandParser {

  private GitCommandParser() {
  }

  static String[] parse(String command) {
    List<String> strs = parseDelimitedString(command, " ", true);
    String[] args = strs.toArray(new String[strs.size()]);
    for (int i = 0; i < args.length; i++) {
      String argVal = args[i];
      if (argVal.startsWith("'") && argVal.endsWith("'")) {
        args[i] = argVal.substring(1, argVal.length() - 1);
        argVal = args[i];
      }
      if (argVal.startsWith("\"") && argVal.endsWith("\"")) {
        args[i] = argVal.substring(1, argVal.length() - 1);
      }
    }

    if (args.length != 2) {
      throw new IllegalArgumentException("Invalid git command line (no arguments): " + command);
    }
    return args;
  }

  private static List<String> parseDelimitedString(String value, String delim, boolean trim) {
    if (value == null) {
      value = "";
    }

    List<String> list = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    int expecting = 7;
    boolean isEscaped = false;

    for(int i = 0; i < value.length(); ++i) {
      char c = value.charAt(i);
      boolean isDelimiter = delim.indexOf(c) >= 0;
      if (!isEscaped && c == '\\') {
        isEscaped = true;
      } else {
        if (isEscaped) {
          sb.append(c);
        } else if (isDelimiter && (expecting & 2) != 0) {
          if (trim) {
            String str = sb.toString();
            list.add(str.trim());
          } else {
            list.add(sb.toString());
          }

          sb.delete(0, sb.length());
          expecting = 7;
        } else if (c == '"' && (expecting & 4) != 0) {
          sb.append(c);
          expecting = 9;
        } else if (c == '"' && (expecting & 8) != 0) {
          sb.append(c);
          expecting = 7;
        } else {
          if ((expecting & 1) == 0) {
            throw new IllegalArgumentException("Invalid delimited string: " + value);
          }

          sb.append(c);
        }

        isEscaped = false;
      }
    }

    if (sb.length() > 0) {
      if (trim) {
        String str = sb.toString();
        list.add(str.trim());
      } else {
        list.add(sb.toString());
      }
    }

    return list;
  }
}
