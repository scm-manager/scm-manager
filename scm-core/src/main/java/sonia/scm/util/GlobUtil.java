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

/**
 * Util for pattern matching with glob
 * (http://en.wikipedia.org/wiki/Glob_%28programming%29) syntax.
 *
 * @since 1.8
 */
public final class GlobUtil
{

  private GlobUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Converts the given glob string to a regular expression string.
   */
  public static String convertGlobToRegEx(String globString)
  {
    globString = globString.trim();

    int strLen = globString.length();
    StringBuilder sb = new StringBuilder(strLen);
    boolean escaping = false;
    int inCurlies = 0;

    for (char currentChar : globString.toCharArray())
    {
      switch (currentChar)
      {
        case '*' :
          if (escaping)
          {
            sb.append("\\*");
          }
          else
          {
            sb.append(".*");
          }

          escaping = false;

          break;

        case '?' :
          if (escaping)
          {
            sb.append("\\?");
          }
          else
          {
            sb.append('.');
          }

          escaping = false;

          break;

        case '.' :
        case '(' :
        case ')' :
        case '+' :
        case '|' :
        case '^' :
        case '$' :
        case '@' :
        case '%' :
          sb.append('\\');
          sb.append(currentChar);
          escaping = false;

          break;

        case '\\' :
          if (escaping)
          {
            sb.append("\\\\");
            escaping = false;
          }
          else
          {
            escaping = true;
          }

          break;

        case '{' :
          if (escaping)
          {
            sb.append("\\{");
          }
          else
          {
            sb.append('(');
            inCurlies++;
          }

          escaping = false;

          break;

        case '}' :
          if ((inCurlies > 0) &&!escaping)
          {
            sb.append(')');
            inCurlies--;
          }
          else if (escaping)
          {
            sb.append("\\}");
          }
          else
          {
            sb.append("}");
          }

          escaping = false;

          break;

        case ',' :
          if ((inCurlies > 0) &&!escaping)
          {
            sb.append('|');
          }
          else if (escaping)
          {
            sb.append("\\,");
          }
          else
          {
            sb.append(",");
          }

          break;

        default :
          escaping = false;
          sb.append(currentChar);
      }
    }

    return sb.toString();
  }

  /**
   * Returns true if the glob string matches the given value.
   *
   *
   * @param glob glob pattern
   * @param value string value
   */
  public static boolean matches(String glob, String value)
  {
    return value.matches(convertGlobToRegEx(glob));
  }
}
