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

//~--- JDK imports ------------------------------------------------------------

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Sebastian Sdorra
 */
public class Util
{

  /** Field description */
  public static final String DATE_PATTERN = "yyyy-MM-dd HH-mm-ss";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param object
   * @param otherObject
   * @param <T>
   *
   * @return
   */
  public static <T extends Comparable> int compare(T object, T otherObject)
  {
    int result = 0;

    if ((object != null) && (otherObject != null))
    {
      result = object.compareTo(otherObject);
    }
    else if ((object == null) && (otherObject != null))
    {
      result = 1;
    }
    else if ((object != null) && (otherObject == null))
    {
      result = -1;
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param collection
   * @param other
   * @param <T>
   *
   * @return
   */
  public static <T> boolean containsOne(Collection<T> collection,
          Collection<T> other)
  {
    boolean result = false;

    for (T item : collection)
    {
      if (other.contains(item))
      {
        result = true;

        break;
      }
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param date
   * @param tz
   *
   * @return
   */
  public static String formatDate(Date date, TimeZone tz)
  {
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

    if (tz != null)
    {
      sdf.setTimeZone(tz);
    }

    return sdf.format(date);
  }

  /**
   * Method description
   *
   *
   * @param date
   *
   * @return
   */
  public static String formatDate(Date date)
  {
    return formatDate(date, null);
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public static String nonNull(String value)
  {
    return (value != null)
           ? value
           : "";
  }

  /**
   * Method description
   *
   *
   * @param dateString
   * @param tz
   *
   * @return
   *
   * @throws ParseException
   */
  public static Date parseDate(String dateString, TimeZone tz)
          throws ParseException
  {
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

    if (tz != null)
    {
      sdf.setTimeZone(tz);
    }

    return sdf.parse(dateString);
  }

  /**
   * Method description
   *
   *
   * @param dateString
   *
   * @return
   *
   * @throws ParseException
   */
  public static Date parseDate(String dateString) throws ParseException
  {
    return parseDate(dateString, null);
  }

  /**
   * Method description
   *
   *
   * @param byteValue
   *
   * @return
   */
  public static String toString(byte[] byteValue)
  {
    StringBuilder buffer = new StringBuilder();

    for (int i = 0; i < byteValue.length; i++)
    {
      int x = byteValue[i] & 0xff;

      if (x < 16)
      {
        buffer.append('0');
      }

      buffer.append(Integer.toString(x, 16));
    }

    return buffer.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public static boolean isEmpty(String value)
  {
    return (value == null) || (value.trim().length() == 0);
  }

  /**
   * Method description
   *
   *
   * @param collection
   *
   * @return
   */
  public static boolean isEmpty(Collection<?> collection)
  {
    return (collection == null) || collection.isEmpty();
  }

  /**
   * Method description
   *
   *
   * @param array
   *
   * @return
   */
  public static boolean isEmpty(Object[] array)
  {
    return (array == null) || (array.length == 0);
  }

  /**
   * Method description
   *
   *
   * @param object
   * @param other
   *
   * @return
   */
  public static boolean isEquals(Object object, Object other)
  {
    return (object == null)
           ? other == null
           : object.equals(other);
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public static boolean isNotEmpty(String value)
  {
    return (value != null) && (value.trim().length() > 0);
  }

  /**
   * Method description
   *
   *
   * @param collection
   *
   * @return
   */
  public static boolean isNotEmpty(Collection<?> collection)
  {
    return (collection != null) &&!collection.isEmpty();
  }

  /**
   * Method description
   *
   *
   * @param array
   *
   * @return
   */
  public static boolean isNotEmpty(Object[] array)
  {
    return (array != null) && (array.length > 0);
  }

  /**
   * Method description
   *
   *
   * @param object
   * @param other
   *
   * @return
   */
  public static boolean isNotEquals(Object object, Object other)
  {
    return !isEquals(object, other);
  }
}
