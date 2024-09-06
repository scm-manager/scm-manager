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
 * @since 1.15
 */
public final class WebUtil
{

  public static final String CACHE_CONTROL_PREVENT =
    "no-cache, must-revalidate";

  public static final String DATE_PREVENT_CACHE =
    "Tue, 09 Apr 1985 10:00:00 GMT";

  public static final String HEADER_ACCEPTENCODING = "Accept-Encoding";

  public static final String HEADER_CACHECONTROL = "Cache-Control";

  public static final String HEADER_ETAG = "Etag";

  public static final String HEADER_EXPIRES = "Expires";

  public static final String HEADER_IFMS = "If-Modified-Since";

  public static final String HEADER_INM = "If-None-Match";

  public static final String HEADER_LASTMODIFIED = "Last-Modified";

  public static final String HEADER_PRAGMA = "Pragma";

  public static final String PRAGMA_NOCACHE = "no-cache";

  public static final long TIME_DAY = 60 * 60 * 24;

  public static final long TIME_MONTH = 60 * 60 * 24 * 30;

  public static final long TIME_YEAR = 60 * 60 * 24 * 365;

  private static final String HTTP_DATE_FORMAT =
    "EEE, dd MMM yyyy HH:mm:ss zzz";

  private static final Logger logger = LoggerFactory.getLogger(WebUtil.class);


  private WebUtil() {}


  public static void addETagHeader(HttpServletResponse response, File file)
  {
    response.addHeader(HEADER_ETAG, getETag(file));
  }


  public static void addPreventCacheHeaders(HttpServletResponse response)
  {
    response.addDateHeader(HEADER_LASTMODIFIED, new Date().getTime());
    response.addHeader(HEADER_CACHECONTROL, CACHE_CONTROL_PREVENT);
    response.addHeader(HEADER_PRAGMA, PRAGMA_NOCACHE);
    response.addHeader(HEADER_EXPIRES, DATE_PREVENT_CACHE);
  }

  public static void addStaticCacheControls(HttpServletResponse response,
    long seconds)
  {
    long time = new Date().getTime();

    response.addDateHeader(HEADER_EXPIRES, time + (seconds * 1000));

    String cc = "max-age=".concat(Long.toString(seconds)).concat(", public");

    // use public for https
    response.addHeader(HEADER_CACHECONTROL, cc);
  }

  public static String formatHttpDate(Date date)
  {
    return getHttpDateFormat().format(date);
  }

  public static Date parseHttpDate(String dateString) throws ParseException
  {
    return getHttpDateFormat().parse(dateString);
  }


  public static String getETag(File file)
  {
    return new StringBuilder("W/\"").append(file.length()).append(
      file.lastModified()).append("\"").toString();
  }


  public static DateFormat getHttpDateFormat()
  {
    SimpleDateFormat dateFormat = new SimpleDateFormat(HTTP_DATE_FORMAT,
                                    Locale.ENGLISH);

    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    return dateFormat;
  }

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

  public static boolean isGzipSupported(HttpServletRequest request)
  {
    return isGzipSupported(request::getHeader);
  }

  public static boolean isGzipSupported(Function<String, String> headerResolver)
  {
    String enc = headerResolver.apply(HEADER_ACCEPTENCODING);

    return (enc != null) && enc.contains("gzip");
  }

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
