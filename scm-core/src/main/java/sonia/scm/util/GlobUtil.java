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

/**
 * Util for pattern matching with glob 
 * (http://en.wikipedia.org/wiki/Glob_%28programming%29) syntax.
 *
 * @author Sebastian Sdorra
 * @since 1.8
 */
public class GlobUtil
{

  /**
   * Converts the given glob string to a regular expression string.
   *
   *
   * @param globString string to convert
   *
   * @return regular expression string
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
   *
   * @return true if the glob string matches the given value
   */
  public static boolean matches(String glob, String value)
  {
    return value.matches(convertGlobToRegEx(value));
  }
}
