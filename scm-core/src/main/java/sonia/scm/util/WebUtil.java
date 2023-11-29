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

//~--- non-JDK imports --------------------------------------------------------

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Function;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.15
 */
public final class WebUtil
{

  /** Field description */
  public static final String CACHE_CONTROL_PREVENT =
    "no-cache, must-revalidate";

  /** Field description */
  public static final String DATE_PREVENT_CACHE =
    "Tue, 09 Apr 1985 10:00:00 GMT";

  /** Field description */
  public static final String HEADER_ACCEPTENCODING = "Accept-Encoding";

  /** Field description */
  public static final String HEADER_CACHECONTROL = "Cache-Control";

  /** Field description */
  public static final String HEADER_ETAG = "Etag";

  /** Field description */
  public static final String HEADER_EXPIRES = "Expires";

  /** Field description */
  public static final String HEADER_IFMS = "If-Modified-Since";

  /** Field description */
  public static final String HEADER_INM = "If-None-Match";

  /** Field description */
  public static final String HEADER_LASTMODIFIED = "Last-Modified";

  /** Field description */
  public static final String HEADER_PRAGMA = "Pragma";

  /** Field description */
  public static final String PRAGMA_NOCACHE = "no-cache";

  /** Field description */
  public static final long TIME_DAY = 60 * 60 * 24;

  /** Field description */
  public static final long TIME_MONTH = 60 * 60 * 24 * 30;

  /** Field description */
  public static final long TIME_YEAR = 60 * 60 * 24 * 365;

  /** Field description */
  private static final String HTTP_DATE_FORMAT =
    "EEE, dd MMM yyyy HH:mm:ss zzz";

  /** Field description */
  private static final Logger logger = LoggerFactory.getLogger(WebUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private WebUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param response
   * @param file
   */
  public static void addETagHeader(HttpServletResponse response, File file)
  {
    response.addHeader(HEADER_ETAG, getETag(file));
  }

  /**
   * Method description
   *
   *
   * @param response
   */
  public static void addPreventCacheHeaders(HttpServletResponse response)
  {
    response.addDateHeader(HEADER_LASTMODIFIED, new Date().getTime());
    response.addHeader(HEADER_CACHECONTROL, CACHE_CONTROL_PREVENT);
    response.addHeader(HEADER_PRAGMA, PRAGMA_NOCACHE);
    response.addHeader(HEADER_EXPIRES, DATE_PREVENT_CACHE);
  }

  /**
   * Method description
   *
   *
   * @param response
   * @param seconds
   */
  public static void addStaticCacheControls(HttpServletResponse response,
    long seconds)
  {
    long time = new Date().getTime();

    response.addDateHeader(HEADER_EXPIRES, time + (seconds * 1000));

    String cc = "max-age=".concat(Long.toString(seconds)).concat(", public");

    // use public for https
    response.addHeader(HEADER_CACHECONTROL, cc);
  }

  /**
   * Method description
   *
   *
   * @param date
   *
   * @return
   */
  public static String formatHttpDate(Date date)
  {
    return getHttpDateFormat().format(date);
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
  public static Date parseHttpDate(String dateString) throws ParseException
  {
    return getHttpDateFormat().parse(dateString);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param file
   *
   * @return
   */
  public static String getETag(File file)
  {
    return new StringBuilder("W/\"").append(file.length()).append(
      file.lastModified()).append("\"").toString();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public static DateFormat getHttpDateFormat()
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat(HTTP_DATE_FORMAT,
                                    Locale.ENGLISH);

    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    return dateFormat;
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  public static Date getIfModifiedSinceDate(HttpServletRequest request)
  {
    Date date = null;
    String dateString = request.getHeader(HEADER_IFMS);

    if ((dateString != null) && (dateString.length() > 0))
    {
      try
      {
        date = parseHttpDate(dateString);
      }
      catch (ParseException ex)
      {
        logger.warn("could not parse http date", ex);
      }
    }

    return date;
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  public static boolean isGzipSupported(HttpServletRequest request)
  {
    return isGzipSupported(request::getHeader);
  }

  public static boolean isGzipSupported(Function<String, String> headerResolver)
  {
    String enc = headerResolver.apply(HEADER_ACCEPTENCODING);

    return (enc != null) && enc.contains("gzip");
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param file
   *
   * @return
   */
  public static boolean isModified(HttpServletRequest request, File file)
  {
    boolean result = true;
    Date modifiedSince = getIfModifiedSinceDate(request);

    if ((modifiedSince != null)
      && (modifiedSince.getTime() == file.lastModified()))
    {
      result = false;
    }

    if (result)
    {
      String inmEtag = request.getHeader(HEADER_INM);

      if ((inmEtag != null) && (inmEtag.length() > 0)
        && inmEtag.equals(getETag(file)))
      {
        result = false;
      }
    }

    return result;
  }
}
