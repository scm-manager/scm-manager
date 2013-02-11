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



package sonia.scm.upgrade;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sebastian Sdorra
 */
public final class ClientDateFormatConverter
{

  /** Field description */
  private static final String SINGLECHAR_REGEX = "(^|[^%s])[%s]($|[^%s])";

  /**
   * the logger for DateFormatConverter
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ClientDateFormatConverter.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private ClientDateFormatConverter() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Documentations:
   * - Extjs: http://trac.geoext.org/browser/ext/3.4.0/docs/source/Date.html
   * - Moments: http://momentjs.com/docs/#/displaying/format
   *
   *
   * @param value
   *
   * @return
   */
  public static String extjsToMoments(String value)
  {
    logger.trace(
      "try to convert extjs date format \"{}\" to moments date format", value);

    String result = replaceDateFormatChars(value, "d", "DD");

    result = replaceDateFormatChars(result, "D", "ddd");
    result = replaceDateFormatChars(result, "j", "D");
    result = replaceDateFormatChars(result, "l", "dddd");

    // no replacement found for 1-7, only 0-6 found
    result = replaceDateFormatChars(result, "N", "d");
    result = replaceDateFormatChars(result, "w", "d");
    result = replaceDateFormatChars(result, "z", "DDDD");
    result = replaceDateFormatChars(result, "W", "ww");
    result = replaceDateFormatChars(result, "M", "MMM");
    result = replaceDateFormatChars(result, "F", "MMMM");
    result = replaceDateFormatChars(result, "m", "MM");
    result = replaceDateFormatChars(result, "n", "M");
    result = replaceDateFormatChars(result, "Y", "YYYY");
    result = replaceDateFormatChars(result, "o", "YYYY");
    result = replaceDateFormatChars(result, "y", "YY");
    result = replaceDateFormatChars(result, "H", "HH");
    result = replaceDateFormatChars(result, "h", "hh");
    result = replaceDateFormatChars(result, "g", "h");
    result = replaceDateFormatChars(result, "G", "H");
    result = replaceDateFormatChars(result, "i", "mm");
    result = replaceDateFormatChars(result, "s", "ss");
    result = replaceDateFormatChars(result, "O", "ZZ");
    result = replaceDateFormatChars(result, "P", "Z");
    result = replaceDateFormatChars(result, "T", "z");

    logger.debug(
      "converted extjs date format \"{}\" to moments date format \"{}\"",
      value, result);

    return result;
  }

  /**
   * Method description
   *
   *
   * @param value
   * @param c
   * @param replacement
   *
   * @return
   */
  private static String replaceDateFormatChars(String value, String c,
    String replacement)
  {
    Pattern p = Pattern.compile(String.format(SINGLECHAR_REGEX, c, c, c));
    StringBuffer buffer = new StringBuffer();
    Matcher m = p.matcher(value);

    while (m.find())
    {
      m.appendReplacement(buffer, "$1" + replacement + "$2");
    }

    m.appendTail(buffer);

    return buffer.toString();
  }
}
