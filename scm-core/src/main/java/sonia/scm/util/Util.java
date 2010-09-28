/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.util;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sebastian Sdorra
 */
public class Util
{

  /** Field description */
  public static final String DATE_PATTERN = "yyyy-MM-dd HH-mm-ss";

  /** Field description */
  private static final Logger logger = Logger.getLogger(Util.class.getName());

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param closeable
   */
  public static void close(Closeable closeable)
  {
    if (closeable != null)
    {
      try
      {
        closeable.close();
      }
      catch (IOException ex)
      {
        logger.log(Level.SEVERE, null, ex);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param in
   * @param out
   *
   * @throws IOException
   */
  public static void copy(InputStream in, OutputStream out) throws IOException
  {
    byte[] buffer = new byte[0xFFFF];

    for (int len; (len = in.read(buffer)) != -1; )
    {
      out.write(buffer, 0, len);
    }
  }

  /**
   *   Method description
   *
   *
   *   @param file
   *
   *   @throws IOException
   */
  public static void delete(File file) throws IOException
  {
    if (file.isDirectory())
    {
      File[] children = file.listFiles();

      if (children != null)
      {
        for (File child : children)
        {
          delete(child);
        }
      }
    }

    if (!file.delete())
    {
      throw new IOException("could not delete file ".concat(file.getPath()));
    }
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
