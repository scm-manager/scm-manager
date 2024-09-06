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
