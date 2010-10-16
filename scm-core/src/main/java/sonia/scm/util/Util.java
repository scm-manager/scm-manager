/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
}
