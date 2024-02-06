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


import com.google.common.base.Strings;
import com.google.common.collect.Multimap;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public final class Util
{

  public static final String DATE_PATTERN = "yyyy-MM-dd HH-mm-ss";

  public static final String EMPTY_STRING = "";


  private Util() {}

  @SuppressWarnings("unchecked")
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

  public static String convertTime(long time)
  {
    String suffix = "ms";

    if (time > 1000)
    {
      time /= 1000;
      suffix = "s";

      if (time > 60)
      {
        time /= 60;
        suffix = "m";

        if (time > 60)
        {
          time /= 60;
          suffix = "h";
        }
      }
    }

    return time + suffix;
  }

  public static long convertTime(String timeString)
  {
    char suffix = timeString.charAt(timeString.length() - 1);
    long time = Long.parseLong(timeString.substring(0,
                  timeString.length() - 1));

    switch (suffix)
    {
      case 'h' :
        time *= 60;
      case 'm' :
        time *= 60;
      case 's' :
        time *= 1000;
    }

    return time;
  }

  /**
   * @since 1.4
   */
  public static <T> Collection<T> createSubCollection(Collection<T> values,
    Comparator<T> comparator, int start, int limit)
  {
    return createSubCollection(values, comparator, null, start, limit);
  }

  /**
   * @since 1.4
   */
  public static <T> Collection<T> createSubCollection(Collection<T> values,
    int start, int limit)
  {
    return createSubCollection(values, null, null, start, limit);
  }

  /**
   * @since 1.4
   */
  public static <T> Collection<T> createSubCollection(Collection<T> values,
    CollectionAppender<T> appender, int start, int limit)
  {
    return createSubCollection(values, null, appender, start, limit);
  }

  /**
   * @since 1.4
   */
  public static <T> Collection<T> createSubCollection(Collection<T> values,
    Comparator<T> comparator, CollectionAppender<T> appender, int start,
    int limit)
  {
    List<T> result = new ArrayList<>();
    List<T> valueList = new ArrayList<>(values);

    if (comparator != null)
    {
      valueList.sort(comparator);
    }

    int length = valueList.size();
    int end = start + limit;

    if (length > start)
    {
      if (length < end)
      {
        end = length;
      }

      valueList = valueList.subList(start, end);

      if (appender == null)
      {
        result = valueList;
      }
      else
      {
        for (T v : valueList)
        {
          appender.append(result, v);
        }
      }
    }

    return result;
  }

  public static String formatDate(Date date, TimeZone tz)
  {
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

    if (tz != null)
    {
      sdf.setTimeZone(tz);
    }

    return sdf.format(date);
  }

  public static String formatDate(Date date)
  {
    return formatDate(date, null);
  }

  public static byte[] fromHexString(String value)
  {
    return new BigInteger(value, 16).toByteArray();
  }

  /**
   * Returns an emtpy string, if the object is null. Otherwise, the result of
   * the toString method of the object is returned.
   *
   * @param value object
   *
   * @since 1.13
   *
   * @return string value or empty string
   */
  public static String nonNull(Object value)
  {
    return (value != null)
      ? value.toString()
      : "";
  }

  /**
   * Returns an empty string, if the string is null. Otherwise, the string
   * is returned. The method is available to fix a possible linkage error which
   * was introduced with version 1.14. Please have a look at:
   * https://bitbucket.org/sdorra/scm-manager/issue/569/active-directory-plugin-not-working-in
   *
   * @param value string value
   *
   * @return string value or empty string
   *
   * @since 1.38
   */
  public static String nonNull(String value)
  {
    return Strings.nullToEmpty(value);
  }

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

  public static Date parseDate(String dateString) throws ParseException
  {
    return parseDate(dateString, null);
  }

  /**
   * Returns the first value of a {@link Multimap} or {@code null}.
   *
   *
   * @param map multi map
   * @param key key
   * @param <K> type of key
   * @param <V> type of
   *
   * @return first value of {@code null}
   * 
   * @since 2.0.0
   */
  public static <K, V> V getFirst(Multimap<K, V> map, K key)
  {
    return map.get(key).iterator().next();
  }

  /**
   * @since 1.17
   */
  public static boolean startWithIgnoreCase(String value, String start)
  {
    return (value != null) && (start != null)
      && value.toUpperCase(Locale.ENGLISH).startsWith(start);
  }

  public static String toString(Collection<? extends Object> collection)
  {
    StringBuilder sb = new StringBuilder();

    if (collection != null)
    {
      Iterator<? extends Object> it = collection.iterator();

      while (it.hasNext())
      {
        sb.append(it.next());

        if (it.hasNext())
        {
          sb.append(", ");
        }
      }
    }

    return sb.toString();
  }

  public static String toString(byte[] byteValue)
  {
    StringBuilder buffer = new StringBuilder();

    for (final byte aByteValue : byteValue) {
      int x = aByteValue & 0xff;

      if (x < 16) {
        buffer.append('0');
      }

      buffer.append(Integer.toString(x, 16));
    }

    return buffer.toString();
  }


  /**
   * @since 1.5
   */
  public static <T> T getFirst(Iterable<T> iterable)
  {
    T result = null;

    if (iterable != null)
    {
      result = getFirst(iterable.iterator());
    }

    return result;
  }

  /**
   * @since 1.5
   */
  public static <T> T getFirst(Iterator<T> iterator)
  {
    T result = null;

    if ((iterator != null) && iterator.hasNext())
    {
      result = iterator.next();
    }

    return result;
  }

  public static boolean isEmpty(String value)
  {
    return (value == null) || (value.trim().length() == 0);
  }

  public static boolean isEmpty(Collection<?> collection)
  {
    return (collection == null) || collection.isEmpty();
  }

  public static boolean isEmpty(Map<?, ?> map)
  {
    return (map == null) || map.isEmpty();
  }

  public static boolean isEmpty(Object[] array)
  {
    return (array == null) || (array.length == 0);
  }

  public static boolean isEquals(Object object, Object other)
  {
    return (object == null)
      ? other == null
      : object.equals(other);
  }

  public static boolean isNotEmpty(String value)
  {
    return (value != null) && (value.trim().length() > 0);
  }

  public static boolean isNotEmpty(Collection<?> collection)
  {
    return (collection != null) &&!collection.isEmpty();
  }

  public static boolean isNotEmpty(Map<?, ?> map)
  {
    return (map != null) &&!map.isEmpty();
  }

  public static boolean isNotEmpty(Object[] array)
  {
    return (array != null) && (array.length > 0);
  }

  public static boolean isNotEquals(Object object, Object other)
  {
    return !isEquals(object, other);
  }
}
