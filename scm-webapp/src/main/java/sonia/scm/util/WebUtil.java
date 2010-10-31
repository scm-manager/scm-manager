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

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public class WebUtil
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
    response.addHeader(HEADER_CACHECONTROL, cc.toString());
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
        logger.warn(ex.getMessage(), ex);
      }
      catch (NumberFormatException ex)
      {
        logger.warn(ex.getMessage(), ex);
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
    String enc = request.getHeader(HEADER_ACCEPTENCODING);

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
